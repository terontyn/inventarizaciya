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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class InventoryApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void readEndpointsReturnTenantScopedWarehousesStoragePlacesAndCurrentInventory() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID warehouseA = insertWarehouse(tenantA);
        UUID storagePlaceA = insertStoragePlace(tenantA, warehouseA);
        UUID assetInStorage = UUID.randomUUID();
        insertInventoryRecord(tenantA, assetInStorage, warehouseA, storagePlaceA);

        UUID warehouseB = insertWarehouse(tenantB);
        insertStoragePlace(tenantB, warehouseB);
        insertInventoryRecord(tenantB, UUID.randomUUID(), warehouseB, null);

        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectAssetLookup(tenantA, supportReviewerId, assetInStorage, HttpStatus.OK);

        mockMvc.perform(get("/api/v1/warehouses")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(warehouseA.toString()));

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
            .andExpect(jsonPath("$[0].storage_place_id").value(storagePlaceA.toString()));

        mockMvc.perform(get("/api/v1/inventory/assets/{assetId}", assetInStorage)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetInStorage.toString()))
            .andExpect(jsonPath("$.in_storage_now").value(true))
            .andExpect(jsonPath("$.warehouse_id").value(warehouseA.toString()))
            .andExpect(jsonPath("$.storage_place_id").value(storagePlaceA.toString()));

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void assetOperatorCanPlaceAssetIntoStorageAndMoveItWithAudit() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetOperatorId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID warehouseA = insertWarehouse(tenantId);
        UUID warehouseB = insertWarehouse(tenantId);
        UUID storagePlaceA = insertStoragePlace(tenantId, warehouseA);
        UUID storagePlaceB = insertStoragePlace(tenantId, warehouseB);

        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, assetOperatorId, assetId, HttpStatus.OK);
        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, assetOperatorId, assetId, HttpStatus.OK);

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
            .andExpect(jsonPath("$.in_storage_now").value(true));

        AuditRow placeAudit = latestAuditRow();
        assertThat(placeAudit.actionType()).isEqualTo("PLACE_IN_STORAGE");
        assertThat(placeAudit.correlationId()).isEqualTo("corr-place");

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
            .andExpect(jsonPath("$.in_storage_now").value(true));

        InventoryRecordRow movedRow = inventoryRecordRow(tenantId, assetId);
        assertThat(movedRow.warehouseId()).isEqualTo(warehouseB);
        assertThat(movedRow.storagePlaceId()).isEqualTo(storagePlaceB);

        AuditRow moveAudit = latestAuditRow();
        assertThat(moveAudit.actionType()).isEqualTo("MOVE");
        assertThat(moveAudit.correlationId()).isEqualTo("corr-move");

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void supportReviewerCanReadButCannotMutateInventory() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID warehouseId = insertWarehouse(tenantId);
        UUID assetId = UUID.randomUUID();

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        mockMvc.perform(post("/api/v1/inventory/assets/{assetId}/place-in-storage", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "support-reviewer")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("warehouse_id", warehouseId))))
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
        UUID warehouseA = insertWarehouse(tenantId);
        UUID warehouseB = insertWarehouse(tenantId);
        UUID storagePlaceB = insertStoragePlace(tenantId, warehouseB);
        insertInventoryRecord(tenantId, assetId, warehouseA, null);

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectAssetLookup(tenantId, tenantAdminId, assetId, HttpStatus.OK);
        UUID secondAssetId = UUID.randomUUID();
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectAssetLookup(tenantId, tenantAdminId, secondAssetId, HttpStatus.OK);
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectAssetLookup(tenantId, tenantAdminId, secondAssetId, HttpStatus.OK);

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
                .content(objectMapper.writeValueAsString(Map.of(
                    "warehouse_id", warehouseA
                ))))
            .andExpect(status().isCreated());

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
            .andExpect(jsonPath("$.storage_place_id").doesNotExist());

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void inventoryEndpointsReturnServiceUnavailableWhenCrossServiceLookupFails() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID callerId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID warehouseId = insertWarehouse(tenantId);

        expectRoleLookupFailure(tenantId, callerId, HttpStatus.INTERNAL_SERVER_ERROR);
        expectRoleLookup(tenantId, callerId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, callerId, assetId, HttpStatus.INTERNAL_SERVER_ERROR);

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

        mockUserService.verify();
        mockAssetService.verify();
    }
}
