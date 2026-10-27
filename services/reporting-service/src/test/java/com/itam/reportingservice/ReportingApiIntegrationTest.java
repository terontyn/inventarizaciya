package com.itam.reportingservice;

import static com.itam.reportingservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.reportingservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.itam.reportingservice.application.PilotRoleNames;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ReportingApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void listEndpointReturnsDerivedCurrentVisibilityAcrossOwnerContexts() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID assetOne = UUID.randomUUID();
        UUID assetTwo = UUID.randomUUID();
        UUID assignedUserId = UUID.randomUUID();
        UUID assignedLocationId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID storagePlaceId = UUID.randomUUID();
        UUID softwareOne = UUID.randomUUID();
        UUID softwareTwo = UUID.randomUUID();

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectAssetList(tenantId, supportReviewerId, List.of(
            new AssetResponse(assetOne, "ASSET-1", "SN-1", "LAPTOP", "ACTIVE", assignedUserId, assignedLocationId),
            new AssetResponse(assetTwo, "ASSET-2", "SN-2", "MONITOR", "ACTIVE", null, assignedLocationId)
        ));

        expectInventorySlice(
            tenantId,
            supportReviewerId,
            assetOne,
            HttpStatus.OK,
            new InventoryAssetResponse(assetOne, true, warehouseId, storagePlaceId, "READY")
        );
        expectMaintenanceSlice(
            tenantId,
            supportReviewerId,
            assetOne,
            HttpStatus.OK,
            new AssetMaintenanceResponse(assetOne, true, false, UUID.randomUUID(), "REPAIR", "OPEN", null, null)
        );
        expectTicketSlice(
            tenantId,
            supportReviewerId,
            assetOne,
            HttpStatus.OK,
            List.of(new TicketResponse(UUID.randomUUID(), assetOne, "OPEN", null, null))
        );
        expectSoftwareSlice(
            tenantId,
            supportReviewerId,
            assetOne,
            HttpStatus.OK,
            List.of(
                new SoftwareRecordSummaryResponse(softwareOne, "Office"),
                new SoftwareRecordSummaryResponse(softwareTwo, "VPN Client")
            )
        );

        expectInventorySlice(
            tenantId,
            supportReviewerId,
            assetTwo,
            HttpStatus.OK,
            new InventoryAssetResponse(assetTwo, false, null, null, null)
        );
        expectMaintenanceSlice(
            tenantId,
            supportReviewerId,
            assetTwo,
            HttpStatus.OK,
            new AssetMaintenanceResponse(assetTwo, false, true, null, null, null, null, null)
        );
        expectTicketSlice(tenantId, supportReviewerId, assetTwo, HttpStatus.OK, List.of());
        expectSoftwareSlice(tenantId, supportReviewerId, assetTwo, HttpStatus.OK, List.of());

        mockMvc.perform(get("/api/v1/reporting/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].asset_id").value(assetOne.toString()))
            .andExpect(jsonPath("$[0].assigned_user_id").value(assignedUserId.toString()))
            .andExpect(jsonPath("$[0].assigned_location_id").value(assignedLocationId.toString()))
            .andExpect(jsonPath("$[0].warehouse_id").value(warehouseId.toString()))
            .andExpect(jsonPath("$[0].storage_place_id").value(storagePlaceId.toString()))
            .andExpect(jsonPath("$[0].in_storage_now").value(true))
            .andExpect(jsonPath("$[0].readiness_state").value("READY"))
            .andExpect(jsonPath("$[0].has_open_maintenance_work").value(true))
            .andExpect(jsonPath("$[0].has_open_tickets").value(true))
            .andExpect(jsonPath("$[0].software_relations.length()").value(2))
            .andExpect(jsonPath("$[0].inventory_slice_available").value(true))
            .andExpect(jsonPath("$[0].maintenance_slice_available").value(true))
            .andExpect(jsonPath("$[0].ticket_slice_available").value(true))
            .andExpect(jsonPath("$[0].software_slice_available").value(true))
            .andExpect(jsonPath("$[1].asset_id").value(assetTwo.toString()))
            .andExpect(jsonPath("$[1].assigned_user_id").value(nullValue()))
            .andExpect(jsonPath("$[1].in_storage_now").value(false))
            .andExpect(jsonPath("$[1].readiness_state").value(nullValue()))
            .andExpect(jsonPath("$[1].has_open_maintenance_work").value(false))
            .andExpect(jsonPath("$[1].has_open_tickets").value(false))
            .andExpect(jsonPath("$[1].software_relations.length()").value(0));

        mockUserService.verify();
        mockAssetService.verify();
        mockInventoryService.verify();
        mockMaintenanceService.verify();
        mockTicketService.verify();
        mockSoftwareService.verify();
    }

    @Test
    void singleAssetEndpointReturnsDerivedCurrentVisibility() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetOperatorId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID assignedUserId = UUID.randomUUID();
        UUID assignedLocationId = UUID.randomUUID();
        UUID softwareId = UUID.randomUUID();

        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetDetail(
            tenantId,
            assetOperatorId,
            new AssetResponse(assetId, "ASSET-9", "SN-9", "TABLET", "ACTIVE", assignedUserId, assignedLocationId),
            HttpStatus.OK
        );
        expectInventorySlice(
            tenantId,
            assetOperatorId,
            assetId,
            HttpStatus.OK,
            new InventoryAssetResponse(assetId, false, null, null, null)
        );
        expectMaintenanceSlice(
            tenantId,
            assetOperatorId,
            assetId,
            HttpStatus.OK,
            new AssetMaintenanceResponse(assetId, false, true, null, null, null, null, null)
        );
        expectTicketSlice(tenantId, assetOperatorId, assetId, HttpStatus.OK, List.of());
        expectSoftwareSlice(
            tenantId,
            assetOperatorId,
            assetId,
            HttpStatus.OK,
            List.of(new SoftwareRecordSummaryResponse(softwareId, "VPN Client"))
        );

        mockMvc.perform(get("/api/v1/reporting/assets/{assetId}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.assigned_user_id").value(assignedUserId.toString()))
            .andExpect(jsonPath("$.assigned_location_id").value(assignedLocationId.toString()))
            .andExpect(jsonPath("$.in_storage_now").value(false))
            .andExpect(jsonPath("$.readiness_state").value(nullValue()))
            .andExpect(jsonPath("$.has_open_maintenance_work").value(false))
            .andExpect(jsonPath("$.has_open_tickets").value(false))
            .andExpect(jsonPath("$.software_relations.length()").value(1))
            .andExpect(jsonPath("$.software_relations[0].software_record_id").value(softwareId.toString()))
            .andExpect(jsonPath("$.software_relations[0].name").value("VPN Client"));

        mockUserService.verify();
        mockAssetService.verify();
        mockInventoryService.verify();
        mockMaintenanceService.verify();
        mockTicketService.verify();
        mockSoftwareService.verify();
    }

    @Test
    void singleAssetEndpointSafelyHandlesPartialCompositionWhenSecondarySourcesFail() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID assignedLocationId = UUID.randomUUID();

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectAssetDetail(
            tenantId,
            supportReviewerId,
            new AssetResponse(assetId, "ASSET-12", "SN-12", "DOCK", "ACTIVE", null, assignedLocationId),
            HttpStatus.OK
        );
        expectInventorySlice(tenantId, supportReviewerId, assetId, HttpStatus.INTERNAL_SERVER_ERROR, null);
        expectMaintenanceSlice(tenantId, supportReviewerId, assetId, HttpStatus.NOT_FOUND, null);
        expectTicketSlice(tenantId, supportReviewerId, assetId, HttpStatus.INTERNAL_SERVER_ERROR, List.of());
        expectSoftwareSlice(tenantId, supportReviewerId, assetId, HttpStatus.NOT_FOUND, List.of());

        mockMvc.perform(get("/api/v1/reporting/assets/{assetId}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.assigned_location_id").value(assignedLocationId.toString()))
            .andExpect(jsonPath("$.inventory_slice_available").value(false))
            .andExpect(jsonPath("$.maintenance_slice_available").value(false))
            .andExpect(jsonPath("$.ticket_slice_available").value(false))
            .andExpect(jsonPath("$.software_slice_available").value(false))
            .andExpect(jsonPath("$.in_storage_now").value(nullValue()))
            .andExpect(jsonPath("$.readiness_state").value(nullValue()))
            .andExpect(jsonPath("$.has_open_maintenance_work").value(nullValue()))
            .andExpect(jsonPath("$.has_open_tickets").value(nullValue()))
            .andExpect(jsonPath("$.software_relations.length()").value(0));

        mockUserService.verify();
        mockAssetService.verify();
        mockInventoryService.verify();
        mockMaintenanceService.verify();
        mockTicketService.verify();
        mockSoftwareService.verify();
    }

    @Test
    void sourceAssetServiceAndRoleLookupFailuresAreProtected() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID callerId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        expectRoleLookupFailure(tenantId, callerId, HttpStatus.INTERNAL_SERVER_ERROR);
        expectRoleLookup(tenantId, callerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectAssetListFailure(tenantId, callerId, HttpStatus.INTERNAL_SERVER_ERROR);
        expectRoleLookup(tenantId, callerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectAssetDetail(
            tenantId,
            callerId,
            new AssetResponse(assetId, "ASSET-404", "SN-404", "LAPTOP", "ACTIVE", null, null),
            HttpStatus.NOT_FOUND
        );

        mockMvc.perform(get("/api/v1/reporting/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId))
            .andExpect(status().isServiceUnavailable());

        mockMvc.perform(get("/api/v1/reporting/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId))
            .andExpect(status().isServiceUnavailable());

        mockMvc.perform(get("/api/v1/reporting/assets/{assetId}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId))
            .andExpect(status().isNotFound());

        mockUserService.verify();
        mockAssetService.verify();
        mockInventoryService.verify();
        mockMaintenanceService.verify();
        mockTicketService.verify();
        mockSoftwareService.verify();
    }

    @Test
    void reportingSurfaceRemainsReadOnlyAndRequiresReadRole() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID noRoleUserId = UUID.randomUUID();

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectAssetList(tenantId, supportReviewerId, List.of());
        expectRoleLookup(tenantId, noRoleUserId, List.of());

        mockMvc.perform(get("/api/v1/reporting/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/v1/reporting/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, noRoleUserId))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/reporting/assets"))
            .andExpect(status().isMethodNotAllowed());

        mockUserService.verify();
        mockAssetService.verify();
        mockInventoryService.verify();
        mockMaintenanceService.verify();
        mockTicketService.verify();
        mockSoftwareService.verify();
    }
}
