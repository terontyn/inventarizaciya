package com.itam.assetservice;

import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_ACTOR_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.CORRELATION_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.itam.assetservice.application.PilotRoleNames;
import com.itam.assetservice.domain.AssetStatus;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "integration.inventory-service.assign-handoff.enabled=true")
class AssetAssignHandoffIntegrationTest extends IntegrationTestSupport {

    @Test
    void successfulAssignTriggersInternalInventoryHandoffAfterCommit() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetOperatorId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null
        );

        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectUserLookup(tenantId, assetOperatorId, targetUserId, HttpStatus.OK);
        expectInventoryAssignHandoff(
            tenantId,
            assetOperatorId,
            assetId,
            "asset-operator",
            "corr-assign-handoff",
            HttpStatus.ACCEPTED
        );

        mockMvc.perform(post("/api/v1/assets/{id}/assign", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .header(CORRELATION_ID_HEADER, "corr-assign-handoff")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_user_id", targetUserId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()))
            .andExpect(jsonPath("$.assigned_user_id").value(targetUserId.toString()));

        mockUserService.verify();
        mockInventoryService.verify();
        assertThat(assetRow(tenantId, assetId).assignedUserId()).isEqualTo(targetUserId);
    }

    @Test
    void assignStillSucceedsWhenInventoryHandoffTemporarilyFails() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectUserLookup(tenantId, tenantAdminId, targetUserId, HttpStatus.OK);
        expectInventoryAssignHandoff(
            tenantId,
            tenantAdminId,
            assetId,
            "tenant-admin",
            "corr-assign-no-rollback",
            HttpStatus.INTERNAL_SERVER_ERROR
        );

        mockMvc.perform(post("/api/v1/assets/{id}/assign", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-assign-no-rollback")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_user_id", targetUserId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()))
            .andExpect(jsonPath("$.assigned_user_id").value(targetUserId.toString()));

        mockUserService.verify();
        mockInventoryService.verify();
        assertThat(assetRow(tenantId, assetId).assignedUserId()).isEqualTo(targetUserId);
        assertThat(latestResponsibilityFactRow().factType()).isEqualTo("ASSIGN");
        assertThat(latestAuditRow().actionType()).isEqualTo("ASSIGN");
    }

    @Test
    void returnDoesNotTriggerInventoryHandoff() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            AssetStatus.ACTIVE,
            UUID.randomUUID(),
            null
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(post("/api/v1/assets/{id}/return", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-return"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()));

        mockUserService.verify();
        mockInventoryService.verify();
        assertThat(assetRow(tenantId, assetId).assignedUserId()).isNull();
    }
}
