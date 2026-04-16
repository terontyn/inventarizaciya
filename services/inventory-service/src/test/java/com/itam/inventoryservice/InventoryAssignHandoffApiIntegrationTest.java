package com.itam.inventoryservice;

import static com.itam.inventoryservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_ACTOR_HEADER;
import static com.itam.inventoryservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.inventoryservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static com.itam.inventoryservice.api.AuthenticatedRequestContextExtractor.CORRELATION_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class InventoryAssignHandoffApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void assignHandoffConsumesCurrentInventoryRecordAndWritesAudit() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID warehouseId = insertWarehouse(tenantId);
        UUID storagePlaceId = insertStoragePlace(tenantId, warehouseId);
        insertInventoryRecord(tenantId, assetId, warehouseId, storagePlaceId);

        mockMvc.perform(post("/internal/api/v1/inventory/assets/{assetId}/assign-handoff", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, UUID.randomUUID())
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-assign-handoff"))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.consumed").value(true));

        assertThat(countRows("inventory_records")).isEqualTo(0);

        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.actionType()).isEqualTo("CONSUME_FROM_ASSIGN");
        assertThat(auditRow.correlationId()).isEqualTo("corr-assign-handoff");
        assertThat(auditRow.changePayload()).contains(assetId.toString());
    }

    @Test
    void replayedAssignHandoffIsIdempotentNoOpWhenCurrentRepresentationAlreadyAbsent() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID sharedAssetId = UUID.randomUUID();
        UUID warehouseB = insertWarehouse(tenantB);
        insertInventoryRecord(tenantB, sharedAssetId, warehouseB, null);

        mockMvc.perform(post("/internal/api/v1/inventory/assets/{assetId}/assign-handoff", sharedAssetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, UUID.randomUUID())
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-noop"))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.asset_id").value(sharedAssetId.toString()))
            .andExpect(jsonPath("$.consumed").value(false));

        assertThat(countRows("inventory_records")).isEqualTo(1);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
        assertThat(inventoryRecordRow(tenantB, sharedAssetId).warehouseId()).isEqualTo(warehouseB);
    }
}
