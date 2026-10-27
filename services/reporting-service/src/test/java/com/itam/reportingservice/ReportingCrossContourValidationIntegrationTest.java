package com.itam.reportingservice;

import static com.itam.reportingservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.reportingservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.itam.reportingservice.application.PilotRoleNames;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ReportingCrossContourValidationIntegrationTest extends IntegrationTestSupport {

    @Test
    void confirmedAlignmentPreservesOwnerSideCurrentStateAcrossAllSlices() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID assignedUserId = UUID.randomUUID();
        UUID assignedLocationId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID storagePlaceId = UUID.randomUUID();
        UUID maintenanceWorkId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID softwareOneId = UUID.randomUUID();
        UUID softwareTwoId = UUID.randomUUID();

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectAssetDetail(
            tenantId,
            supportReviewerId,
            new AssetResponse(
                assetId,
                "ASSET-ALIGN",
                "SN-ALIGN",
                "LAPTOP",
                "ACTIVE",
                assignedUserId,
                assignedLocationId
            ),
            HttpStatus.OK
        );
        expectInventorySlice(
            tenantId,
            supportReviewerId,
            assetId,
            HttpStatus.OK,
            new InventoryAssetResponse(assetId, true, warehouseId, storagePlaceId, "NOT_READY")
        );
        expectMaintenanceSlice(
            tenantId,
            supportReviewerId,
            assetId,
            HttpStatus.OK,
            new AssetMaintenanceResponse(
                assetId,
                true,
                false,
                maintenanceWorkId,
                "INSPECTION",
                "OPEN",
                "2026-04-21T10:15:30Z",
                null
            )
        );
        expectTicketSlice(
            tenantId,
            supportReviewerId,
            assetId,
            HttpStatus.OK,
            List.of(new TicketResponse(ticketId, assetId, "OPEN", "2026-04-21T10:20:00Z", null))
        );
        expectSoftwareSlice(
            tenantId,
            supportReviewerId,
            assetId,
            HttpStatus.OK,
            List.of(
                new SoftwareRecordSummaryResponse(softwareOneId, "Office"),
                new SoftwareRecordSummaryResponse(softwareTwoId, "VPN Client")
            )
        );

        mockMvc.perform(get("/api/v1/reporting/assets/{assetId}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.assigned_user_id").value(assignedUserId.toString()))
            .andExpect(jsonPath("$.assigned_location_id").value(assignedLocationId.toString()))
            .andExpect(jsonPath("$.warehouse_id").value(warehouseId.toString()))
            .andExpect(jsonPath("$.storage_place_id").value(storagePlaceId.toString()))
            .andExpect(jsonPath("$.in_storage_now").value(true))
            .andExpect(jsonPath("$.readiness_state").value("NOT_READY"))
            .andExpect(jsonPath("$.has_open_maintenance_work").value(true))
            .andExpect(jsonPath("$.has_open_tickets").value(true))
            .andExpect(jsonPath("$.software_relations.length()").value(2))
            .andExpect(jsonPath("$.software_relations[0].software_record_id").value(softwareOneId.toString()))
            .andExpect(jsonPath("$.software_relations[0].name").value("Office"))
            .andExpect(jsonPath("$.software_relations[1].software_record_id").value(softwareTwoId.toString()))
            .andExpect(jsonPath("$.software_relations[1].name").value("VPN Client"))
            .andExpect(jsonPath("$.inventory_slice_available").value(true))
            .andExpect(jsonPath("$.maintenance_slice_available").value(true))
            .andExpect(jsonPath("$.ticket_slice_available").value(true))
            .andExpect(jsonPath("$.software_slice_available").value(true));

        mockUserService.verify();
        mockAssetService.verify();
        mockInventoryService.verify();
        mockMaintenanceService.verify();
        mockTicketService.verify();
        mockSoftwareService.verify();
    }

    @Test
    void acceptedLagKeepsMixedAssignAndInventoryPictureWithoutSyntheticRepair() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID assignedUserId = UUID.randomUUID();
        UUID assignedLocationId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID storagePlaceId = UUID.randomUUID();

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectAssetDetail(
            tenantId,
            supportReviewerId,
            new AssetResponse(
                assetId,
                "ASSET-LAG",
                "SN-LAG",
                "TABLET",
                "ACTIVE",
                assignedUserId,
                assignedLocationId
            ),
            HttpStatus.OK
        );
        expectInventorySlice(
            tenantId,
            supportReviewerId,
            assetId,
            HttpStatus.OK,
            new InventoryAssetResponse(assetId, true, warehouseId, storagePlaceId, "READY")
        );
        expectMaintenanceSlice(
            tenantId,
            supportReviewerId,
            assetId,
            HttpStatus.OK,
            new AssetMaintenanceResponse(assetId, false, true, null, null, null, null, null)
        );
        expectTicketSlice(tenantId, supportReviewerId, assetId, HttpStatus.OK, List.of());
        expectSoftwareSlice(tenantId, supportReviewerId, assetId, HttpStatus.OK, List.of());

        mockMvc.perform(get("/api/v1/reporting/assets/{assetId}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.assigned_user_id").value(assignedUserId.toString()))
            .andExpect(jsonPath("$.assigned_location_id").value(assignedLocationId.toString()))
            .andExpect(jsonPath("$.warehouse_id").value(warehouseId.toString()))
            .andExpect(jsonPath("$.storage_place_id").value(storagePlaceId.toString()))
            .andExpect(jsonPath("$.in_storage_now").value(true))
            .andExpect(jsonPath("$.readiness_state").value("READY"))
            .andExpect(jsonPath("$.has_open_maintenance_work").value(false))
            .andExpect(jsonPath("$.has_open_tickets").value(false))
            .andExpect(jsonPath("$.software_relations.length()").value(0))
            .andExpect(jsonPath("$.inventory_slice_available").value(true))
            .andExpect(jsonPath("$.maintenance_slice_available").value(true))
            .andExpect(jsonPath("$.ticket_slice_available").value(true))
            .andExpect(jsonPath("$.software_slice_available").value(true));

        mockUserService.verify();
        mockAssetService.verify();
        mockInventoryService.verify();
        mockMaintenanceService.verify();
        mockTicketService.verify();
        mockSoftwareService.verify();
    }

    @Test
    void safePartialCompositionDropsOnlyUnavailableSecondarySliceAndKeepsOthersIntact() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID inventoryGapAssetId = UUID.randomUUID();
        UUID maintenanceGapAssetId = UUID.randomUUID();
        UUID ticketGapAssetId = UUID.randomUUID();
        UUID softwareGapAssetId = UUID.randomUUID();

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectAssetList(
            tenantId,
            supportReviewerId,
            List.of(
                new AssetResponse(inventoryGapAssetId, "ASSET-INV-GAP", "SN-INV-GAP", "LAPTOP", "ACTIVE", null, null),
                new AssetResponse(maintenanceGapAssetId, "ASSET-MAINT-GAP", "SN-MAINT-GAP", "MONITOR", "ACTIVE", null, null),
                new AssetResponse(ticketGapAssetId, "ASSET-TICKET-GAP", "SN-TICKET-GAP", "DOCK", "ACTIVE", null, null),
                new AssetResponse(softwareGapAssetId, "ASSET-SOFT-GAP", "SN-SOFT-GAP", "PHONE", "ACTIVE", null, null)
            )
        );

        expectInventorySlice(tenantId, supportReviewerId, inventoryGapAssetId, HttpStatus.INTERNAL_SERVER_ERROR, null);
        expectMaintenanceSlice(
            tenantId,
            supportReviewerId,
            inventoryGapAssetId,
            HttpStatus.OK,
            new AssetMaintenanceResponse(inventoryGapAssetId, false, true, null, null, null, null, null)
        );
        expectTicketSlice(
            tenantId,
            supportReviewerId,
            inventoryGapAssetId,
            HttpStatus.OK,
            List.of(new TicketResponse(UUID.randomUUID(), inventoryGapAssetId, "OPEN", "2026-04-21T12:00:00Z", null))
        );
        expectSoftwareSlice(
            tenantId,
            supportReviewerId,
            inventoryGapAssetId,
            HttpStatus.OK,
            List.of(new SoftwareRecordSummaryResponse(UUID.randomUUID(), "Inventory Agent"))
        );

        expectInventorySlice(
            tenantId,
            supportReviewerId,
            maintenanceGapAssetId,
            HttpStatus.OK,
            new InventoryAssetResponse(maintenanceGapAssetId, false, null, null, null)
        );
        expectMaintenanceSlice(tenantId, supportReviewerId, maintenanceGapAssetId, HttpStatus.INTERNAL_SERVER_ERROR, null);
        expectTicketSlice(
            tenantId,
            supportReviewerId,
            maintenanceGapAssetId,
            HttpStatus.OK,
            List.of(new TicketResponse(UUID.randomUUID(), maintenanceGapAssetId, "OPEN", "2026-04-21T12:05:00Z", null))
        );
        expectSoftwareSlice(
            tenantId,
            supportReviewerId,
            maintenanceGapAssetId,
            HttpStatus.OK,
            List.of(new SoftwareRecordSummaryResponse(UUID.randomUUID(), "Display Driver"))
        );

        expectInventorySlice(
            tenantId,
            supportReviewerId,
            ticketGapAssetId,
            HttpStatus.OK,
            new InventoryAssetResponse(ticketGapAssetId, true, UUID.randomUUID(), UUID.randomUUID(), "READY")
        );
        expectMaintenanceSlice(
            tenantId,
            supportReviewerId,
            ticketGapAssetId,
            HttpStatus.OK,
            new AssetMaintenanceResponse(ticketGapAssetId, true, false, UUID.randomUUID(), "REPAIR", "OPEN", null, null)
        );
        expectTicketSlice(tenantId, supportReviewerId, ticketGapAssetId, HttpStatus.INTERNAL_SERVER_ERROR, List.of());
        expectSoftwareSlice(
            tenantId,
            supportReviewerId,
            ticketGapAssetId,
            HttpStatus.OK,
            List.of(new SoftwareRecordSummaryResponse(UUID.randomUUID(), "VPN Client"))
        );

        expectInventorySlice(
            tenantId,
            supportReviewerId,
            softwareGapAssetId,
            HttpStatus.OK,
            new InventoryAssetResponse(softwareGapAssetId, false, null, null, null)
        );
        expectMaintenanceSlice(
            tenantId,
            supportReviewerId,
            softwareGapAssetId,
            HttpStatus.OK,
            new AssetMaintenanceResponse(softwareGapAssetId, false, true, null, null, null, null, null)
        );
        expectTicketSlice(
            tenantId,
            supportReviewerId,
            softwareGapAssetId,
            HttpStatus.OK,
            List.of(new TicketResponse(UUID.randomUUID(), softwareGapAssetId, "OPEN", "2026-04-21T12:10:00Z", null))
        );
        expectSoftwareSlice(tenantId, supportReviewerId, softwareGapAssetId, HttpStatus.INTERNAL_SERVER_ERROR, List.of());

        mockMvc.perform(get("/api/v1/reporting/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(4))
            .andExpect(jsonPath("$[0].asset_id").value(inventoryGapAssetId.toString()))
            .andExpect(jsonPath("$[0].inventory_slice_available").value(false))
            .andExpect(jsonPath("$[0].maintenance_slice_available").value(true))
            .andExpect(jsonPath("$[0].ticket_slice_available").value(true))
            .andExpect(jsonPath("$[0].software_slice_available").value(true))
            .andExpect(jsonPath("$[0].in_storage_now").value(nullValue()))
            .andExpect(jsonPath("$[0].readiness_state").value(nullValue()))
            .andExpect(jsonPath("$[0].has_open_maintenance_work").value(false))
            .andExpect(jsonPath("$[0].has_open_tickets").value(true))
            .andExpect(jsonPath("$[0].software_relations.length()").value(1))
            .andExpect(jsonPath("$[1].asset_id").value(maintenanceGapAssetId.toString()))
            .andExpect(jsonPath("$[1].inventory_slice_available").value(true))
            .andExpect(jsonPath("$[1].maintenance_slice_available").value(false))
            .andExpect(jsonPath("$[1].ticket_slice_available").value(true))
            .andExpect(jsonPath("$[1].software_slice_available").value(true))
            .andExpect(jsonPath("$[1].in_storage_now").value(false))
            .andExpect(jsonPath("$[1].readiness_state").value(nullValue()))
            .andExpect(jsonPath("$[1].has_open_maintenance_work").value(nullValue()))
            .andExpect(jsonPath("$[1].has_open_tickets").value(true))
            .andExpect(jsonPath("$[1].software_relations.length()").value(1))
            .andExpect(jsonPath("$[2].asset_id").value(ticketGapAssetId.toString()))
            .andExpect(jsonPath("$[2].inventory_slice_available").value(true))
            .andExpect(jsonPath("$[2].maintenance_slice_available").value(true))
            .andExpect(jsonPath("$[2].ticket_slice_available").value(false))
            .andExpect(jsonPath("$[2].software_slice_available").value(true))
            .andExpect(jsonPath("$[2].in_storage_now").value(true))
            .andExpect(jsonPath("$[2].readiness_state").value("READY"))
            .andExpect(jsonPath("$[2].has_open_maintenance_work").value(true))
            .andExpect(jsonPath("$[2].has_open_tickets").value(nullValue()))
            .andExpect(jsonPath("$[2].software_relations.length()").value(1))
            .andExpect(jsonPath("$[3].asset_id").value(softwareGapAssetId.toString()))
            .andExpect(jsonPath("$[3].inventory_slice_available").value(true))
            .andExpect(jsonPath("$[3].maintenance_slice_available").value(true))
            .andExpect(jsonPath("$[3].ticket_slice_available").value(true))
            .andExpect(jsonPath("$[3].software_slice_available").value(false))
            .andExpect(jsonPath("$[3].in_storage_now").value(false))
            .andExpect(jsonPath("$[3].readiness_state").value(nullValue()))
            .andExpect(jsonPath("$[3].has_open_maintenance_work").value(false))
            .andExpect(jsonPath("$[3].has_open_tickets").value(true))
            .andExpect(jsonPath("$[3].software_relations.length()").value(0));

        mockUserService.verify();
        mockAssetService.verify();
        mockInventoryService.verify();
        mockMaintenanceService.verify();
        mockTicketService.verify();
        mockSoftwareService.verify();
    }
}
