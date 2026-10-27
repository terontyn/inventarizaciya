package com.itam.inventoryservice;

import static com.itam.inventoryservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_ACTOR_HEADER;
import static com.itam.inventoryservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.inventoryservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static com.itam.inventoryservice.api.AuthenticatedRequestContextExtractor.CORRELATION_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.itam.inventoryservice.application.PilotRoleNames;
import com.itam.inventoryservice.domain.ReadinessState;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class InventoryApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void readEndpointsReturnTenantScopedReadinessAwareInventoryAndWarehouseAnchoring() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID branchA = UUID.randomUUID();
        UUID locationA = UUID.randomUUID();
        UUID warehouseA = insertWarehouse(tenantA, branchA, locationA);
        UUID storagePlaceA = insertStoragePlace(tenantA, warehouseA);
        UUID assetInStorage = UUID.randomUUID();
        insertInventoryRecord(tenantA, assetInStorage, warehouseA, storagePlaceA, ReadinessState.READY);

        UUID warehouseB = insertWarehouse(tenantB, UUID.randomUUID());
        insertStoragePlace(tenantB, warehouseB);
        insertInventoryRecord(tenantB, UUID.randomUUID(), warehouseB, null, ReadinessState.NOT_READY);

        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectBranchLookup(tenantA, supportReviewerId, branchA, HttpStatus.OK);
        expectLocationLookup(tenantA, supportReviewerId, locationA, HttpStatus.OK);

        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectBranchLookup(tenantA, supportReviewerId, branchA, HttpStatus.OK);
        expectLocationLookup(tenantA, supportReviewerId, locationA, HttpStatus.OK);

        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectBranchLookup(tenantA, supportReviewerId, branchA, HttpStatus.OK);
        expectLocationLookup(tenantA, supportReviewerId, locationA, HttpStatus.OK);

        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectAssetLookup(tenantA, supportReviewerId, assetInStorage, HttpStatus.OK);
        expectBranchLookup(tenantA, supportReviewerId, branchA, HttpStatus.OK);
        expectLocationLookup(tenantA, supportReviewerId, locationA, HttpStatus.OK);

        mockMvc.perform(get("/api/v1/warehouses")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(warehouseA.toString()))
            .andExpect(jsonPath("$[0].branch_id").value(branchA.toString()))
            .andExpect(jsonPath("$[0].location_id").value(locationA.toString()));

        mockMvc.perform(get("/api/v1/warehouses/{id}/storage-places", warehouseA)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(storagePlaceA.toString()))
            .andExpect(jsonPath("$[0].warehouse_id").value(warehouseA.toString()));

        mockMvc.perform(get("/api/v1/inventory/assets/in-storage")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].asset_id").value(assetInStorage.toString()))
            .andExpect(jsonPath("$[0].in_storage_now").value(true))
            .andExpect(jsonPath("$[0].warehouse_id").value(warehouseA.toString()))
            .andExpect(jsonPath("$[0].storage_place_id").value(storagePlaceA.toString()))
            .andExpect(jsonPath("$[0].readiness_state").value("READY"));

        mockMvc.perform(get("/api/v1/inventory/assets/{assetId}", assetInStorage)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetInStorage.toString()))
            .andExpect(jsonPath("$.in_storage_now").value(true))
            .andExpect(jsonPath("$.warehouse_id").value(warehouseA.toString()))
            .andExpect(jsonPath("$.storage_place_id").value(storagePlaceA.toString()))
            .andExpect(jsonPath("$.readiness_state").value("READY"));

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void assetOperatorCanPlaceIntoStorageChangeReadinessAndMoveWithoutImplicitReset() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetOperatorId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID branchA = UUID.randomUUID();
        UUID branchB = UUID.randomUUID();
        UUID warehouseA = insertWarehouse(tenantId, branchA);
        UUID warehouseB = insertWarehouse(tenantId, branchB);
        UUID storagePlaceA = insertStoragePlace(tenantId, warehouseA);
        UUID storagePlaceB = insertStoragePlace(tenantId, warehouseB);

        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, assetOperatorId, assetId, HttpStatus.OK);
        expectBranchLookup(tenantId, assetOperatorId, branchA, HttpStatus.OK);
        expectBranchLookup(tenantId, assetOperatorId, branchA, HttpStatus.OK);

        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, assetOperatorId, assetId, HttpStatus.OK);
        expectBranchLookup(tenantId, assetOperatorId, branchA, HttpStatus.OK);

        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, assetOperatorId, assetId, HttpStatus.OK);
        expectBranchLookup(tenantId, assetOperatorId, branchB, HttpStatus.OK);
        expectBranchLookup(tenantId, assetOperatorId, branchB, HttpStatus.OK);

        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, assetOperatorId, assetId, HttpStatus.OK);
        expectBranchLookup(tenantId, assetOperatorId, branchB, HttpStatus.OK);

        mockMvc.perform(post("/api/v1/inventory/assets/{assetId}/place-in-storage", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .header(CORRELATION_ID_HEADER, "corr-place")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "warehouse_id", warehouseA,
                    "storage_place_id", storagePlaceA
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.warehouse_id").value(warehouseA.toString()))
            .andExpect(jsonPath("$.storage_place_id").value(storagePlaceA.toString()))
            .andExpect(jsonPath("$.in_storage_now").value(true))
            .andExpect(jsonPath("$.readiness_state").value("NOT_READY"));

        AuditRow placeAudit = latestAuditRow();
        assertThat(placeAudit.actionType()).isEqualTo("PLACE_IN_STORAGE");
        assertThat(placeAudit.correlationId()).isEqualTo("corr-place");
        assertThat(placeAudit.changePayload()).contains("NOT_READY");

        mockMvc.perform(post("/api/v1/inventory/assets/{assetId}/set-readiness", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .header(CORRELATION_ID_HEADER, "corr-ready")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("readiness_state", "READY"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.readiness_state").value("READY"));

        AuditRow readyAudit = latestAuditRow();
        assertThat(readyAudit.actionType()).isEqualTo("SET_READINESS");
        assertThat(readyAudit.correlationId()).isEqualTo("corr-ready");
        assertThat(readyAudit.changePayload()).contains("NOT_READY");
        assertThat(readyAudit.changePayload()).contains("READY");

        mockMvc.perform(post("/api/v1/inventory/assets/{assetId}/move", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .header(CORRELATION_ID_HEADER, "corr-move")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "warehouse_id", warehouseB,
                    "storage_place_id", storagePlaceB
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.warehouse_id").value(warehouseB.toString()))
            .andExpect(jsonPath("$.storage_place_id").value(storagePlaceB.toString()))
            .andExpect(jsonPath("$.in_storage_now").value(true))
            .andExpect(jsonPath("$.readiness_state").value("READY"));

        InventoryRecordRow movedRow = inventoryRecordRow(tenantId, assetId);
        assertThat(movedRow.warehouseId()).isEqualTo(warehouseB);
        assertThat(movedRow.storagePlaceId()).isEqualTo(storagePlaceB);
        assertThat(movedRow.readinessState()).isEqualTo(ReadinessState.READY);

        AuditRow moveAudit = latestAuditRow();
        assertThat(moveAudit.actionType()).isEqualTo("MOVE");
        assertThat(moveAudit.correlationId()).isEqualTo("corr-move");

        mockMvc.perform(post("/api/v1/inventory/assets/{assetId}/set-readiness", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .header(CORRELATION_ID_HEADER, "corr-not-ready")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("readiness_state", "NOT_READY"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.readiness_state").value("NOT_READY"));

        InventoryRecordRow notReadyRow = inventoryRecordRow(tenantId, assetId);
        assertThat(notReadyRow.readinessState()).isEqualTo(ReadinessState.NOT_READY);

        AuditRow notReadyAudit = latestAuditRow();
        assertThat(notReadyAudit.actionType()).isEqualTo("SET_READINESS");
        assertThat(notReadyAudit.correlationId()).isEqualTo("corr-not-ready");
        assertThat(notReadyAudit.changePayload()).contains("READY");
        assertThat(notReadyAudit.changePayload()).contains("NOT_READY");

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void supportReviewerCanReadButCannotMutateInventoryOrReadiness() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID warehouseId = insertWarehouse(tenantId, UUID.randomUUID());
        UUID assetId = UUID.randomUUID();

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));

        mockMvc.perform(post("/api/v1/inventory/assets/{assetId}/place-in-storage", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "support-reviewer")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("warehouse_id", warehouseId))))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/inventory/assets/{assetId}/set-readiness", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "support-reviewer")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("readiness_state", "READY"))))
            .andExpect(status().isForbidden());

        mockUserService.verify();
        mockAssetService.verify();
        assertThat(countRows("inventory_records")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void placeInStorageRejectsDuplicateCurrentRepresentationAndWarehouseStoragePlaceMismatch() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID branchA = UUID.randomUUID();
        UUID branchB = UUID.randomUUID();
        UUID warehouseA = insertWarehouse(tenantId, branchA);
        UUID warehouseB = insertWarehouse(tenantId, branchB);
        UUID storagePlaceB = insertStoragePlace(tenantId, warehouseB);
        insertInventoryRecord(tenantId, assetId, warehouseA, null);

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectAssetLookup(tenantId, tenantAdminId, assetId, HttpStatus.OK);
        expectBranchLookup(tenantId, tenantAdminId, branchA, HttpStatus.OK);

        UUID secondAssetId = UUID.randomUUID();
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectAssetLookup(tenantId, tenantAdminId, secondAssetId, HttpStatus.OK);
        expectBranchLookup(tenantId, tenantAdminId, branchA, HttpStatus.OK);
        expectBranchLookup(tenantId, tenantAdminId, branchA, HttpStatus.OK);

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectAssetLookup(tenantId, tenantAdminId, secondAssetId, HttpStatus.OK);
        expectBranchLookup(tenantId, tenantAdminId, branchA, HttpStatus.OK);

        mockMvc.perform(post("/api/v1/inventory/assets/{assetId}/place-in-storage", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "warehouse_id", warehouseA,
                    "storage_place_id", storagePlaceB
                ))))
            .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/inventory/assets/{assetId}/place-in-storage", secondAssetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("warehouse_id", warehouseA))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.readiness_state").value("NOT_READY"));

        mockMvc.perform(post("/api/v1/inventory/assets/{assetId}/place-in-storage", secondAssetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("warehouse_id", warehouseA))))
            .andExpect(status().isConflict());

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void singleAssetViewReturnsNotInStorageWhenTrackedAssetHasNoCurrentInventoryRecord() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectAssetLookup(tenantId, supportReviewerId, assetId, HttpStatus.OK);

        mockMvc.perform(get("/api/v1/inventory/assets/{assetId}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.in_storage_now").value(false))
            .andExpect(jsonPath("$.warehouse_id").doesNotExist())
            .andExpect(jsonPath("$.storage_place_id").doesNotExist())
            .andExpect(jsonPath("$.readiness_state").doesNotExist());

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void readinessMutationRejectsWhenAssetIsNotCurrentlyInStorage() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetOperatorId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, assetOperatorId, assetId, HttpStatus.OK);

        mockMvc.perform(post("/api/v1/inventory/assets/{assetId}/set-readiness", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("readiness_state", "READY"))))
            .andExpect(status().isNotFound());

        mockUserService.verify();
        mockAssetService.verify();
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void inventoryEndpointsReturnServiceUnavailableWhenCrossServiceLookupFails() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID callerId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID warehouseId = insertWarehouse(tenantId, branchId);

        expectRoleLookupFailure(tenantId, callerId, HttpStatus.INTERNAL_SERVER_ERROR);

        expectRoleLookup(tenantId, callerId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, callerId, assetId, HttpStatus.INTERNAL_SERVER_ERROR);

        expectRoleLookup(tenantId, callerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectBranchLookup(tenantId, callerId, branchId, HttpStatus.INTERNAL_SERVER_ERROR);

        mockMvc.perform(get("/api/v1/warehouses")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId))
            .andExpect(status().isServiceUnavailable());

        mockMvc.perform(post("/api/v1/inventory/assets/{assetId}/place-in-storage", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("warehouse_id", warehouseId))))
            .andExpect(status().isServiceUnavailable());

        mockMvc.perform(get("/api/v1/warehouses")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId))
            .andExpect(status().isServiceUnavailable());

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void warehouseReadRejectsInvalidBranchMapping() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID invalidBranchId = UUID.randomUUID();
        insertWarehouse(tenantId, invalidBranchId);

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectBranchLookup(tenantId, supportReviewerId, invalidBranchId, HttpStatus.NOT_FOUND);

        mockMvc.perform(get("/api/v1/warehouses")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isConflict());

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void warehouseReadRejectsInvalidOptionalLocationMapping() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID invalidLocationId = UUID.randomUUID();
        insertWarehouse(tenantId, branchId, invalidLocationId);

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectBranchLookup(tenantId, supportReviewerId, branchId, HttpStatus.OK);
        expectLocationLookup(tenantId, supportReviewerId, invalidLocationId, HttpStatus.NOT_FOUND);

        mockMvc.perform(get("/api/v1/warehouses")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isConflict());

        mockUserService.verify();
        mockAssetService.verify();
    }
}
