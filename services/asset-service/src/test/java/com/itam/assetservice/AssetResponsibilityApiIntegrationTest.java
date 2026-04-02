package com.itam.assetservice;

import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_ACTOR_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.CORRELATION_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.itam.assetservice.application.PilotRoleNames;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class AssetResponsibilityApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void assignSuccessUpdatesMasterStatePersistsFactAndGeneratesAudit() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_STOCK",
            null,
            locationId
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectUserLookup(tenantId, tenantAdminId, targetUserId, HttpStatus.OK);

        mockMvc.perform(post("/api/v1/assets/{id}/assign", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-assign")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_user_id", targetUserId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()))
            .andExpect(jsonPath("$.assigned_user_id").value(targetUserId.toString()));

        mockUserService.verify();

        AssetRow assetRow = assetRow(tenantId, assetId);
        assertThat(assetRow.assignedUserId()).isEqualTo(targetUserId);
        assertThat(assetRow.locationId()).isEqualTo(locationId);
        assertThat(countRows("asset_responsibility_facts")).isEqualTo(1);
        ResponsibilityFactRow factRow = latestResponsibilityFactRow();
        assertThat(factRow.tenantId()).isEqualTo(tenantId);
        assertThat(factRow.assetId()).isEqualTo(assetId);
        assertThat(factRow.factType()).isEqualTo("ASSIGN");
        assertThat(factRow.previousAssignedUserId()).isNull();
        assertThat(factRow.newAssignedUserId()).isEqualTo(targetUserId);
        assertThat(factRow.previousLocationId()).isEqualTo(locationId);
        assertThat(factRow.newLocationId()).isEqualTo(locationId);

        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.actionType()).isEqualTo("ASSIGN");
        assertThat(auditRow.correlationId()).isEqualTo("corr-assign");
        assertThat(auditRow.changePayload()).contains("\"assigned_user_id\":\"" + targetUserId + "\"");
    }

    @Test
    void assignRejectsCrossTenantOrMissingTargetUser() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_STOCK",
            null,
            null
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectUserLookup(tenantId, tenantAdminId, targetUserId, HttpStatus.NOT_FOUND);

        mockMvc.perform(post("/api/v1/assets/{id}/assign", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_user_id", targetUserId))))
            .andExpect(status().isBadRequest());

        mockUserService.verify();
        assertThat(assetRow(tenantId, assetId).assignedUserId()).isNull();
        assertThat(countRows("asset_responsibility_facts")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void assetOperatorCanRunAssignTransferAndReturn() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetOperatorId = UUID.randomUUID();
        UUID assignedUserId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID transferUserId = UUID.randomUUID();
        UUID initialLocationId = UUID.randomUUID();
        UUID transferLocationId = UUID.randomUUID();
        UUID assignAssetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_STOCK",
            null,
            initialLocationId
        );
        UUID transferAssetId = insertAsset(
            tenantId,
            "LAP-002",
            "SN-002",
            "Laptop",
            "IN_USE",
            currentUserId,
            initialLocationId
        );

        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectUserLookup(tenantId, assetOperatorId, assignedUserId, HttpStatus.OK);
        mockMvc.perform(post("/api/v1/assets/{id}/assign", assignAssetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_user_id", assignedUserId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assigned_user_id").value(assignedUserId.toString()));

        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectUserLookup(tenantId, assetOperatorId, transferUserId, HttpStatus.OK);
        expectLocationLookup(tenantId, assetOperatorId, transferLocationId, HttpStatus.OK);
        mockMvc.perform(post("/api/v1/assets/{id}/transfer", transferAssetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "target_user_id", transferUserId,
                    "target_location_id", transferLocationId
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assigned_user_id").value(transferUserId.toString()))
            .andExpect(jsonPath("$.location_id").value(transferLocationId.toString()));

        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        mockMvc.perform(post("/api/v1/assets/{id}/return", transferAssetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .header(CORRELATION_ID_HEADER, "corr-return-operator"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(transferAssetId.toString()));

        mockUserService.verify();
        assertThat(assetRow(tenantId, assignAssetId).assignedUserId()).isEqualTo(assignedUserId);
        assertThat(assetRow(tenantId, transferAssetId).assignedUserId()).isNull();
        assertThat(assetRow(tenantId, transferAssetId).locationId()).isEqualTo(transferLocationId);
        assertThat(countRows("asset_responsibility_facts")).isEqualTo(3);
        assertThat(latestAuditRow().actionType()).isEqualTo("RETURN");
    }

    @Test
    void transferUserOnlySuccess() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_USE",
            currentUserId,
            locationId
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectUserLookup(tenantId, tenantAdminId, targetUserId, HttpStatus.OK);

        mockMvc.perform(post("/api/v1/assets/{id}/transfer", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-transfer-user")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_user_id", targetUserId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()))
            .andExpect(jsonPath("$.assigned_user_id").value(targetUserId.toString()))
            .andExpect(jsonPath("$.location_id").value(locationId.toString()));

        mockUserService.verify();

        ResponsibilityFactRow factRow = latestResponsibilityFactRow();
        assertThat(factRow.factType()).isEqualTo("TRANSFER");
        assertThat(factRow.previousAssignedUserId()).isEqualTo(currentUserId);
        assertThat(factRow.newAssignedUserId()).isEqualTo(targetUserId);
        assertThat(factRow.previousLocationId()).isEqualTo(locationId);
        assertThat(factRow.newLocationId()).isEqualTo(locationId);

        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.actionType()).isEqualTo("TRANSFER");
        assertThat(auditRow.correlationId()).isEqualTo("corr-transfer-user");
    }

    @Test
    void transferLocationOnlySuccessWithoutCurrentAssignedUser() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID currentLocationId = UUID.randomUUID();
        UUID targetLocationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_STOCK",
            null,
            currentLocationId
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectLocationLookup(tenantId, tenantAdminId, targetLocationId, HttpStatus.OK);

        String responseBody = mockMvc.perform(post("/api/v1/assets/{id}/transfer", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-transfer-location")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_location_id", targetLocationId))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        mockUserService.verify();

        JsonNode responseJson = objectMapper.readTree(responseBody);
        assertThat(responseJson.get("id").asText()).isEqualTo(assetId.toString());
        assertThat(responseJson.get("assigned_user_id").isNull()).isTrue();
        assertThat(responseJson.get("location_id").asText()).isEqualTo(targetLocationId.toString());

        AssetRow assetRow = assetRow(tenantId, assetId);
        assertThat(assetRow.assignedUserId()).isNull();
        assertThat(assetRow.locationId()).isEqualTo(targetLocationId);
        ResponsibilityFactRow factRow = latestResponsibilityFactRow();
        assertThat(factRow.factType()).isEqualTo("TRANSFER");
        assertThat(factRow.previousAssignedUserId()).isNull();
        assertThat(factRow.newAssignedUserId()).isNull();
        assertThat(factRow.previousLocationId()).isEqualTo(currentLocationId);
        assertThat(factRow.newLocationId()).isEqualTo(targetLocationId);
    }

    @Test
    void transferRejectsNoEffectiveChange() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID currentLocationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_USE",
            currentUserId,
            currentLocationId
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectUserLookup(tenantId, tenantAdminId, currentUserId, HttpStatus.OK);
        expectLocationLookup(tenantId, tenantAdminId, currentLocationId, HttpStatus.OK);

        mockMvc.perform(post("/api/v1/assets/{id}/transfer", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "target_user_id", currentUserId,
                    "target_location_id", currentLocationId
                ))))
            .andExpect(status().isBadRequest());

        mockUserService.verify();
        assertThat(countRows("asset_responsibility_facts")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void transferRejectsCreatingFirstUserResponsibility() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_STOCK",
            null,
            null
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectUserLookup(tenantId, tenantAdminId, targetUserId, HttpStatus.OK);

        mockMvc.perform(post("/api/v1/assets/{id}/transfer", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_user_id", targetUserId))))
            .andExpect(status().isBadRequest());

        mockUserService.verify();
        assertThat(assetRow(tenantId, assetId).assignedUserId()).isNull();
        assertThat(countRows("asset_responsibility_facts")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void transferRejectsMissingOrCrossTenantTargetLocation() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID currentLocationId = UUID.randomUUID();
        UUID targetLocationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_USE",
            currentUserId,
            currentLocationId
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectLocationLookup(tenantId, tenantAdminId, targetLocationId, HttpStatus.NOT_FOUND);

        mockMvc.perform(post("/api/v1/assets/{id}/transfer", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_location_id", targetLocationId))))
            .andExpect(status().isBadRequest());

        mockUserService.verify();
        assertThat(assetRow(tenantId, assetId).locationId()).isEqualTo(currentLocationId);
        assertThat(countRows("asset_responsibility_facts")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void returnSuccessClearsAssignedUserWithoutChangingLocation() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_USE",
            currentUserId,
            locationId
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(post("/api/v1/assets/{id}/return", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-return"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()));

        AssetRow assetRow = assetRow(tenantId, assetId);
        assertThat(assetRow.assignedUserId()).isNull();
        assertThat(assetRow.locationId()).isEqualTo(locationId);
        ResponsibilityFactRow factRow = latestResponsibilityFactRow();
        assertThat(factRow.factType()).isEqualTo("RETURN");
        assertThat(factRow.previousAssignedUserId()).isEqualTo(currentUserId);
        assertThat(factRow.newAssignedUserId()).isNull();
        assertThat(factRow.previousLocationId()).isEqualTo(locationId);
        assertThat(factRow.newLocationId()).isEqualTo(locationId);

        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.actionType()).isEqualTo("RETURN");
        assertThat(auditRow.correlationId()).isEqualTo("corr-return");

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        String assetBody = mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        mockUserService.verify();

        JsonNode assetJson = objectMapper.readTree(assetBody);
        assertThat(assetJson.get("assigned_user_id").isNull()).isTrue();
        assertThat(assetJson.get("location_id").asText()).isEqualTo(locationId.toString());
    }

    @Test
    void returnRejectsWhenThereIsNoCurrentAssignedUser() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_STOCK",
            null,
            UUID.randomUUID()
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(post("/api/v1/assets/{id}/return", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin"))
            .andExpect(status().isBadRequest());

        mockUserService.verify();
        assertThat(countRows("asset_responsibility_facts")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void commandEndpointsStayInsideTenantScope() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID tenantBAdminId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantA,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_USE",
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        expectRoleLookup(tenantB, tenantBAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(post("/api/v1/assets/{id}/assign", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_USER_HEADER, tenantBAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_user_id", UUID.randomUUID()))))
            .andExpect(status().isNotFound());

        expectRoleLookup(tenantB, tenantBAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(post("/api/v1/assets/{id}/transfer", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_USER_HEADER, tenantBAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_location_id", UUID.randomUUID()))))
            .andExpect(status().isNotFound());

        expectRoleLookup(tenantB, tenantBAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(post("/api/v1/assets/{id}/return", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_USER_HEADER, tenantBAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin"))
            .andExpect(status().isNotFound());

        mockUserService.verify();
        assertThat(countRows("asset_responsibility_facts")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }
}
