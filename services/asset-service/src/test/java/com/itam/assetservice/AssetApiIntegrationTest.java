package com.itam.assetservice;

import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_ACTOR_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.CORRELATION_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

class AssetApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void tenantAdminCanManageAssetsAndReadsStayTenantScopedWithAudit() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID tenantAAdminId = UUID.randomUUID();
        UUID tenantBAdminId = UUID.randomUUID();
        UUID assignedUserId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();

        expectRoleLookup(tenantA, tenantAAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectUserLookup(tenantA, tenantAAdminId, assignedUserId, HttpStatus.OK);
        expectLocationLookup(tenantA, tenantAAdminId, locationId, HttpStatus.OK);

        String createBody = mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, tenantAAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-asset-create")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001",
                    "serial_number", "SN-001",
                    "asset_type", "Laptop",
                    "status", "IN_STOCK",
                    "assigned_user_id", assignedUserId,
                    "location_id", locationId
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.asset_tag").value("LAP-001"))
            .andExpect(jsonPath("$.assigned_user_id").value(assignedUserId.toString()))
            .andExpect(jsonPath("$.location_id").value(locationId.toString()))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode createJson = objectMapper.readTree(createBody);
        UUID assetId = UUID.fromString(createJson.get("id").asText());

        expectRoleLookup(tenantA, tenantAAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(patch("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, tenantAAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-asset-update")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001-UPDATED",
                    "serial_number", "SN-001-UPDATED",
                    "asset_type", "Laptop Pro",
                    "status", "IN_USE"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()))
            .andExpect(jsonPath("$.asset_tag").value("LAP-001-UPDATED"))
            .andExpect(jsonPath("$.serial_number").value("SN-001-UPDATED"))
            .andExpect(jsonPath("$.asset_type").value("Laptop Pro"))
            .andExpect(jsonPath("$.status").value("IN_USE"))
            .andExpect(jsonPath("$.assigned_user_id").value(assignedUserId.toString()))
            .andExpect(jsonPath("$.location_id").value(locationId.toString()));

        expectRoleLookup(tenantA, tenantAAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(get("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, tenantAAdminId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(assetId.toString()))
            .andExpect(jsonPath("$[0].asset_tag").value("LAP-001-UPDATED"));

        expectRoleLookup(tenantA, tenantAAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, tenantAAdminId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()))
            .andExpect(jsonPath("$.assigned_user_id").value(assignedUserId.toString()))
            .andExpect(jsonPath("$.location_id").value(locationId.toString()));

        expectRoleLookup(tenantB, tenantBAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(get("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_USER_HEADER, tenantBAdminId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        expectRoleLookup(tenantB, tenantBAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_USER_HEADER, tenantBAdminId))
            .andExpect(status().isNotFound());

        mockUserService.verify();
        assertThat(countRows("assets")).isEqualTo(1);
        assertThat(countRows("local_audit_records")).isEqualTo(2);
        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.tenantId()).isEqualTo(tenantA);
        assertThat(auditRow.entityType()).isEqualTo("ASSET");
        assertThat(auditRow.actionType()).isEqualTo("UPDATE");
        assertThat(auditRow.correlationId()).isEqualTo("corr-asset-update");
        assertThat(auditRow.changePayload()).contains("\"assigned_user_id\":\"" + assignedUserId + "\"");
        assertThat(auditRow.changePayload()).contains("\"location_id\":\"" + locationId + "\"");
    }

    @Test
    void assetOperatorCanCreateAndUpdateAssets() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetOperatorId = UUID.randomUUID();

        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        String createBody = mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001",
                    "serial_number", "SN-001",
                    "asset_type", "Laptop",
                    "status", "IN_STOCK"
                ))))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        UUID assetId = UUID.fromString(objectMapper.readTree(createBody).get("id").asText());

        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        mockMvc.perform(patch("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001-UPDATED",
                    "serial_number", "SN-001-UPDATED",
                    "asset_type", "Laptop Pro",
                    "status", "IN_USE"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()))
            .andExpect(jsonPath("$.asset_tag").value("LAP-001-UPDATED"));

        mockUserService.verify();
    }

    @Test
    void supportReviewerCanReadButCannotMutateAssets() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_STOCK",
            null,
            null
        );

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        mockMvc.perform(get("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()));

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "support-reviewer")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-002",
                    "serial_number", "SN-002",
                    "asset_type", "Laptop",
                    "status", "IN_STOCK"
                ))))
            .andExpect(status().isForbidden());

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        mockMvc.perform(post("/api/v1/assets/{id}/assign", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "support-reviewer")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_user_id", UUID.randomUUID()))))
            .andExpect(status().isForbidden());

        mockUserService.verify();
    }

    @Test
    void allDefaultPilotRolesAreForbiddenFromRetire() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_USE",
            UUID.randomUUID(),
            UUID.randomUUID()
        );
        UUID tenantAdminId = UUID.randomUUID();
        UUID assetOperatorId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(post("/api/v1/assets/{id}/retire", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin"))
            .andExpect(status().isForbidden());

        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        mockMvc.perform(post("/api/v1/assets/{id}/retire", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator"))
            .andExpect(status().isForbidden());

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        mockMvc.perform(post("/api/v1/assets/{id}/retire", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "support-reviewer"))
            .andExpect(status().isForbidden());

        mockUserService.verify();
        assertThat(countRows("asset_responsibility_facts")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void missingAuthenticatedUserIdIsRejected() throws Exception {
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId))
            .andExpect(status().isBadRequest());
    }

    @Test
    void roleLookupFailureReturnsServiceUnavailable() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();

        expectRoleLookupFailure(tenantId, tenantAdminId, HttpStatus.INTERNAL_SERVER_ERROR);

        mockMvc.perform(get("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId))
            .andExpect(status().isServiceUnavailable());

        mockUserService.verify();
    }

    @Test
    void assetTagIsUniqueWithinTenantButReusableAcrossTenants() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID tenantAAdminId = UUID.randomUUID();
        UUID tenantBAdminId = UUID.randomUUID();

        expectRoleLookup(tenantA, tenantAAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, tenantAAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001",
                    "serial_number", "SN-001",
                    "asset_type", "Laptop",
                    "status", "IN_STOCK"
                ))))
            .andExpect(status().isCreated());

        expectRoleLookup(tenantA, tenantAAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, tenantAAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001",
                    "serial_number", "SN-002",
                    "asset_type", "Laptop",
                    "status", "IN_STOCK"
                ))))
            .andExpect(status().isConflict());

        expectRoleLookup(tenantB, tenantBAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_USER_HEADER, tenantBAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001",
                    "serial_number", "SN-003",
                    "asset_type", "Laptop",
                    "status", "IN_STOCK"
                ))))
            .andExpect(status().isCreated());

        mockUserService.verify();
    }

    @Test
    void createAssetRejectsAssignedUserOutsideTenantScope() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assignedUserId = UUID.randomUUID();

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectUserLookup(tenantId, tenantAdminId, assignedUserId, HttpStatus.NOT_FOUND);

        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001",
                    "serial_number", "SN-001",
                    "asset_type", "Laptop",
                    "status", "IN_STOCK",
                    "assigned_user_id", assignedUserId
                ))))
            .andExpect(status().isBadRequest());

        mockUserService.verify();
        assertThat(countRows("assets")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void createAssetRejectsLocationOutsideTenantScope() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assignedUserId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectUserLookup(tenantId, tenantAdminId, assignedUserId, HttpStatus.OK);
        expectLocationLookup(tenantId, tenantAdminId, locationId, HttpStatus.NOT_FOUND);

        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001",
                    "serial_number", "SN-001",
                    "asset_type", "Laptop",
                    "status", "IN_STOCK",
                    "assigned_user_id", assignedUserId,
                    "location_id", locationId
                ))))
            .andExpect(status().isBadRequest());

        mockUserService.verify();
        assertThat(countRows("assets")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void patchRejectsHistorySensitiveFields() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assignedUserId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_STOCK",
            assignedUserId,
            locationId
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(patch("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001",
                    "serial_number", "SN-001",
                    "asset_type", "Laptop",
                    "status", "IN_STOCK",
                    "assigned_user_id", UUID.randomUUID()
                ))))
            .andExpect(status().isBadRequest());

        mockUserService.verify();
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void crossTenantUpdateIsRejected() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID tenantBAdminId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantA,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_STOCK",
            null,
            null
        );

        expectRoleLookup(tenantB, tenantBAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(patch("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_USER_HEADER, tenantBAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001-UPDATED",
                    "serial_number", "SN-001-UPDATED",
                    "asset_type", "Laptop Pro",
                    "status", "IN_USE"
                ))))
            .andExpect(status().isNotFound());

        mockUserService.verify();
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }
}
