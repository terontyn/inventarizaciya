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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.itam.assetservice.application.PilotRoleNames;
import com.itam.assetservice.domain.AssetStatus;
import java.util.LinkedHashMap;
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
        expectRoleLookup(tenantA, tenantAAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantA, tenantAAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantA, tenantAAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantB, tenantBAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantB, tenantBAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
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
                    "status", "BROKEN",
                    "assigned_user_id", assignedUserId,
                    "location_id", locationId
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.asset_tag").value("LAP-001"))
            .andExpect(jsonPath("$.status").value(AssetStatus.ACTIVE))
            .andExpect(jsonPath("$.assigned_user_id").value(assignedUserId.toString()))
            .andExpect(jsonPath("$.location_id").value(locationId.toString()))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode createJson = objectMapper.readTree(createBody);
        UUID assetId = UUID.fromString(createJson.get("id").asText());

        mockMvc.perform(patch("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, tenantAAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-asset-update")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001-UPDATED",
                    "serial_number", "SN-001-UPDATED",
                    "asset_type", "Laptop Pro"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()))
            .andExpect(jsonPath("$.asset_tag").value("LAP-001-UPDATED"))
            .andExpect(jsonPath("$.serial_number").value("SN-001-UPDATED"))
            .andExpect(jsonPath("$.asset_type").value("Laptop Pro"))
            .andExpect(jsonPath("$.status").value(AssetStatus.ACTIVE))
            .andExpect(jsonPath("$.assigned_user_id").value(assignedUserId.toString()))
            .andExpect(jsonPath("$.location_id").value(locationId.toString()));

        mockMvc.perform(get("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, tenantAAdminId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(assetId.toString()))
            .andExpect(jsonPath("$[0].asset_tag").value("LAP-001-UPDATED"))
            .andExpect(jsonPath("$[0].status").value(AssetStatus.ACTIVE));

        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, tenantAAdminId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()))
            .andExpect(jsonPath("$.status").value(AssetStatus.ACTIVE))
            .andExpect(jsonPath("$.assigned_user_id").value(assignedUserId.toString()))
            .andExpect(jsonPath("$.location_id").value(locationId.toString()));

        mockMvc.perform(get("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_USER_HEADER, tenantBAdminId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_USER_HEADER, tenantBAdminId))
            .andExpect(status().isNotFound());

        mockUserService.verify();
        assertThat(assetRow(tenantA, assetId).status()).isEqualTo(AssetStatus.ACTIVE);
        assertThat(countRows("assets")).isEqualTo(1);
        assertThat(countRows("local_audit_records")).isEqualTo(2);
        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.tenantId()).isEqualTo(tenantA);
        assertThat(auditRow.entityType()).isEqualTo("ASSET");
        assertThat(auditRow.actionType()).isEqualTo("UPDATE");
        assertThat(auditRow.correlationId()).isEqualTo("corr-asset-update");
        assertThat(auditRow.changePayload()).contains("\"status\":\"" + AssetStatus.ACTIVE + "\"");
    }

    @Test
    void assetOperatorCanCreateAndUpdateAssets() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetOperatorId = UUID.randomUUID();

        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        String createBody = mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001",
                    "serial_number", "SN-001",
                    "asset_type", "Laptop"
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(AssetStatus.ACTIVE))
            .andReturn()
            .getResponse()
            .getContentAsString();

        UUID assetId = UUID.fromString(objectMapper.readTree(createBody).get("id").asText());

        mockMvc.perform(patch("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001-UPDATED",
                    "serial_number", "SN-001-UPDATED",
                    "asset_type", "Laptop Pro"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()))
            .andExpect(jsonPath("$.asset_tag").value("LAP-001-UPDATED"))
            .andExpect(jsonPath("$.status").value(AssetStatus.ACTIVE));

        mockUserService.verify();
        assertThat(assetRow(tenantId, assetId).status()).isEqualTo(AssetStatus.ACTIVE);
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
            AssetStatus.ACTIVE,
            null,
            null
        );

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        mockMvc.perform(get("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()))
            .andExpect(jsonPath("$.status").value(AssetStatus.ACTIVE));

        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "support-reviewer")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-002",
                    "serial_number", "SN-002",
                    "asset_type", "Laptop"
                ))))
            .andExpect(status().isForbidden());

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
    void callersWithoutApprovedPilotRolesCannotReadOrMutateAssets() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID callerId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null
        );

        expectRoleLookup(tenantId, callerId, List.of());
        expectRoleLookup(tenantId, callerId, List.of());
        expectRoleLookup(tenantId, callerId, List.of());

        mockMvc.perform(get("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "no-approved-role")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-002",
                    "serial_number", "SN-002",
                    "asset_type", "Laptop"
                ))))
            .andExpect(status().isForbidden());

        mockUserService.verify();
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void allDefaultPilotRolesAreForbiddenFromRetire() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            AssetStatus.ACTIVE,
            UUID.randomUUID(),
            UUID.randomUUID()
        );
        UUID tenantAdminId = UUID.randomUUID();
        UUID assetOperatorId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        mockMvc.perform(post("/api/v1/assets/{id}/retire", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/assets/{id}/retire", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/assets/{id}/retire", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "support-reviewer"))
            .andExpect(status().isForbidden());

        mockUserService.verify();
        assertThat(assetRow(tenantId, assetId).status()).isEqualTo(AssetStatus.ACTIVE);
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
    void missingAuthenticatedTenantIdIsRejected() throws Exception {
        UUID authenticatedUserId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/assets")
                .header(AUTHENTICATED_USER_HEADER, authenticatedUserId))
            .andExpect(status().isBadRequest());
    }

    @Test
    void invalidAuthenticatedTenantIdIsRejected() throws Exception {
        UUID authenticatedUserId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, "not-a-uuid")
                .header(AUTHENTICATED_USER_HEADER, authenticatedUserId))
            .andExpect(status().isBadRequest());
    }

    @Test
    void invalidAuthenticatedUserIdIsRejected() throws Exception {
        UUID tenantId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, "not-a-uuid"))
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
        expectRoleLookup(tenantA, tenantAAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantB, tenantBAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, tenantAAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001",
                    "serial_number", "SN-001",
                    "asset_type", "Laptop"
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(AssetStatus.ACTIVE));

        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, tenantAAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001",
                    "serial_number", "SN-002",
                    "asset_type", "Laptop"
                ))))
            .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_USER_HEADER, tenantBAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001",
                    "serial_number", "SN-003",
                    "asset_type", "Laptop"
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(AssetStatus.ACTIVE));

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
                    "assigned_user_id", assignedUserId,
                    "location_id", locationId
                ))))
            .andExpect(status().isBadRequest());

        mockUserService.verify();
        assertThat(countRows("assets")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void patchCannotBeUsedToRetireAssetViaStatusField() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
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
        mockMvc.perform(patch("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001-UPDATED",
                    "serial_number", "SN-001-UPDATED",
                    "asset_type", "Laptop Pro",
                    "status", AssetStatus.RETIRED
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(AssetStatus.ACTIVE));

        mockUserService.verify();
        assertThat(assetRow(tenantId, assetId).status()).isEqualTo(AssetStatus.ACTIVE);
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
            AssetStatus.ACTIVE,
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
                    "assigned_user_id", UUID.randomUUID()
                ))))
            .andExpect(status().isBadRequest());

        mockUserService.verify();
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void patchRejectsLocationIdAsHistorySensitiveField() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assignedUserId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            AssetStatus.ACTIVE,
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
                    "location_id", UUID.randomUUID()
                ))))
            .andExpect(status().isBadRequest());

        mockUserService.verify();
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void createAssetStoresIdentityFieldsAndResponsesIncludeThem() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        Map<String, Object> createPayload = assetPayload("LAP-IDENTITY", "SN-IDENTITY", "Laptop");
        createPayload.put("display_name", "  Dell XPS\n13  ");
        createPayload.put("note", "  First deployment\nKeep charger nearby  ");

        String createBody = mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-identity-create")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPayload)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.asset_tag").value("LAP-IDENTITY"))
            .andExpect(jsonPath("$.display_name").value("Dell XPS 13"))
            .andExpect(jsonPath("$.note").value("First deployment\nKeep charger nearby"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        UUID assetId = UUID.fromString(objectMapper.readTree(createBody).get("id").asText());
        assertThat(assetRow(tenantId, assetId).displayName()).isEqualTo("Dell XPS 13");
        assertThat(assetRow(tenantId, assetId).note()).isEqualTo("First deployment\nKeep charger nearby");

        mockMvc.perform(get("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].display_name").value("Dell XPS 13"))
            .andExpect(jsonPath("$[0].note").value("First deployment\nKeep charger nearby"));

        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.display_name").value("Dell XPS 13"))
            .andExpect(jsonPath("$.note").value("First deployment\nKeep charger nearby"));

        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.actionType()).isEqualTo("CREATE");
        assertThat(auditRow.correlationId()).isEqualTo("corr-identity-create");
        assertThat(auditRow.changePayload())
            .contains("\"display_name\":\"Dell XPS 13\"")
            .contains("\"note\":\"First deployment\\nKeep charger nearby\"");
        mockUserService.verify();
    }

    @Test
    void createAssetNormalizesNullAndBlankIdentityFieldsToNull() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        Map<String, Object> nullPayload = assetPayload("LAP-NULL", "SN-NULL", "Laptop");
        nullPayload.put("display_name", null);
        nullPayload.put("note", null);
        String nullBody = mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullPayload)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode nullJson = objectMapper.readTree(nullBody);
        UUID nullAssetId = UUID.fromString(nullJson.get("id").asText());
        assertThat(nullJson.get("display_name").isNull()).isTrue();
        assertThat(nullJson.get("note").isNull()).isTrue();
        assertThat(assetRow(tenantId, nullAssetId).displayName()).isNull();
        assertThat(assetRow(tenantId, nullAssetId).note()).isNull();

        Map<String, Object> blankPayload = assetPayload("LAP-BLANK", "SN-BLANK", "Laptop");
        blankPayload.put("display_name", "   \n  ");
        blankPayload.put("note", "   ");
        String blankBody = mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blankPayload)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode blankJson = objectMapper.readTree(blankBody);
        UUID blankAssetId = UUID.fromString(blankJson.get("id").asText());
        assertThat(blankJson.get("display_name").isNull()).isTrue();
        assertThat(blankJson.get("note").isNull()).isTrue();
        assertThat(assetRow(tenantId, blankAssetId).displayName()).isNull();
        assertThat(assetRow(tenantId, blankAssetId).note()).isNull();
        mockUserService.verify();
    }

    @Test
    void createAssetRejectsTooLongIdentityFields() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        Map<String, Object> longDisplayNamePayload = assetPayload("LAP-LONG-NAME", "SN-LONG-NAME", "Laptop");
        longDisplayNamePayload.put("display_name", "d".repeat(161));
        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(longDisplayNamePayload)))
            .andExpect(status().isBadRequest());

        Map<String, Object> longNotePayload = assetPayload("LAP-LONG-NOTE", "SN-LONG-NOTE", "Laptop");
        longNotePayload.put("note", "n".repeat(2001));
        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(longNotePayload)))
            .andExpect(status().isBadRequest());

        mockUserService.verify();
        assertThat(countRows("assets")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void patchUpdatesPreservesAndClearsIdentityFields() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-IDENTITY",
            "SN-IDENTITY",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null,
            "Original name",
            "Original note"
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        Map<String, Object> displayNamePatch = assetPayload("LAP-IDENTITY", "SN-IDENTITY", "Laptop");
        displayNamePatch.put("display_name", "  Updated\nname  ");
        mockMvc.perform(patch("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(displayNamePatch)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.display_name").value("Updated name"))
            .andExpect(jsonPath("$.note").value("Original note"));

        Map<String, Object> notePatch = assetPayload("LAP-IDENTITY", "SN-IDENTITY", "Laptop");
        notePatch.put("note", "  Updated\nnote  ");
        mockMvc.perform(patch("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notePatch)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.display_name").value("Updated name"))
            .andExpect(jsonPath("$.note").value("Updated\nnote"));

        Map<String, Object> nullPatch = assetPayload("LAP-IDENTITY", "SN-IDENTITY", "Laptop");
        nullPatch.put("display_name", null);
        nullPatch.put("note", null);
        String nullPatchBody = mockMvc.perform(patch("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullPatch)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        JsonNode nullPatchJson = objectMapper.readTree(nullPatchBody);
        assertThat(nullPatchJson.get("display_name").isNull()).isTrue();
        assertThat(nullPatchJson.get("note").isNull()).isTrue();

        Map<String, Object> restorePatch = assetPayload("LAP-IDENTITY", "SN-IDENTITY", "Laptop");
        restorePatch.put("display_name", "Restored name");
        restorePatch.put("note", "Restored note");
        mockMvc.perform(patch("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(restorePatch)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.display_name").value("Restored name"))
            .andExpect(jsonPath("$.note").value("Restored note"));

        Map<String, Object> blankPatch = assetPayload("LAP-IDENTITY", "SN-IDENTITY", "Laptop");
        blankPatch.put("display_name", "   \n ");
        blankPatch.put("note", "   ");
        String blankPatchBody = mockMvc.perform(patch("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-identity-update")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blankPatch)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        JsonNode blankPatchJson = objectMapper.readTree(blankPatchBody);
        assertThat(blankPatchJson.get("display_name").isNull()).isTrue();
        assertThat(blankPatchJson.get("note").isNull()).isTrue();
        assertThat(assetRow(tenantId, assetId).displayName()).isNull();
        assertThat(assetRow(tenantId, assetId).note()).isNull();

        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.actionType()).isEqualTo("UPDATE");
        assertThat(auditRow.correlationId()).isEqualTo("corr-identity-update");
        assertThat(auditRow.changePayload())
            .contains("\"display_name\":null")
            .contains("\"note\":null");
        mockUserService.verify();
    }

    @Test
    void identityFieldsRemainTenantScopedForReadAndUpdate() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID tenantBAdminId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantA,
            "LAP-IDENTITY",
            "SN-IDENTITY",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null,
            "Tenant A name",
            "Tenant A note"
        );

        expectRoleLookup(tenantB, tenantBAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantB, tenantBAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_USER_HEADER, tenantBAdminId))
            .andExpect(status().isNotFound());

        Map<String, Object> patchPayload = assetPayload("LAP-IDENTITY-B", "SN-IDENTITY-B", "Laptop Pro");
        patchPayload.put("display_name", "Tenant B name");
        patchPayload.put("note", "Tenant B note");
        mockMvc.perform(patch("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_USER_HEADER, tenantBAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchPayload)))
            .andExpect(status().isNotFound());

        assertThat(assetRow(tenantA, assetId).displayName()).isEqualTo("Tenant A name");
        assertThat(assetRow(tenantA, assetId).note()).isEqualTo("Tenant A note");
        mockUserService.verify();
    }

    @Test
    void technicalAttributesDetailReadUpdateAndAuditForPcLaptopProfile() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-TECH",
            "SN-TECH",
            "  laptop  ",
            AssetStatus.ACTIVE,
            null,
            null
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.technical_attributes.profile").value("PC_LAPTOP"))
            .andExpect(jsonPath("$.technical_attributes.values").isEmpty());

        Map<String, Object> values = new LinkedHashMap<>();
        values.put("hostname", "  host-01  ");
        values.put("anydesk_id", "123456789");
        values.put("ip_address", "10.0.0.25");
        values.put("mac_address", "AA:BB:CC:DD:EE:FF");
        mockMvc.perform(put("/api/v1/assets/{id}/technical-attributes", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-tech-update")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("values", values))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.asset_tag").value("LAP-TECH"))
            .andExpect(jsonPath("$.profile").value("PC_LAPTOP"))
            .andExpect(jsonPath("$.values.hostname").value("host-01"))
            .andExpect(jsonPath("$.values.anydesk_id").value("123456789"))
            .andExpect(jsonPath("$.values.ip_address").value("10.0.0.25"))
            .andExpect(jsonPath("$.values.mac_address").value("AA:BB:CC:DD:EE:FF"));

        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.technical_attributes.profile").value("PC_LAPTOP"))
            .andExpect(jsonPath("$.technical_attributes.values.hostname").value("host-01"))
            .andExpect(jsonPath("$.technical_attributes.values.anydesk_id").value("123456789"))
            .andExpect(jsonPath("$.technical_attributes.values.ip_address").value("10.0.0.25"))
            .andExpect(jsonPath("$.technical_attributes.values.mac_address").value("AA:BB:CC:DD:EE:FF"));

        mockMvc.perform(get("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].technical_attributes").doesNotExist());

        assertThat(technicalAttributeValue(tenantId, assetId, "hostname")).isEqualTo("host-01");
        assertThat(countRows("local_audit_records")).isEqualTo(1);
        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.actionType()).isEqualTo("ASSET_TECHNICAL_ATTRIBUTES_UPDATED");
        assertThat(auditRow.correlationId()).isEqualTo("corr-tech-update");
        JsonNode payload = objectMapper.readTree(auditRow.changePayload());
        assertThat(payload.get("asset_id").asText()).isEqualTo(assetId.toString());
        assertThat(payload.get("asset_tag").asText()).isEqualTo("LAP-TECH");
        assertThat(payload.get("profile").asText()).isEqualTo("PC_LAPTOP");
        assertThat(payload.get("changed_keys").toString())
            .contains("hostname")
            .contains("anydesk_id")
            .contains("ip_address")
            .contains("mac_address");
        assertThat(payload.get("before").get("hostname").isNull()).isTrue();
        assertThat(payload.get("after").get("hostname").asText()).isEqualTo("host-01");
        assertThat(payload.get("sensitivity_note").asText()).contains("operational network");
        mockUserService.verify();
    }

    @Test
    void technicalAttributesSupportPhoneAndPrinterProfiles() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID phoneAssetId = insertAsset(
            tenantId,
            "PHONE-TECH",
            "SN-PHONE",
            "  IP PHONE  ",
            AssetStatus.ACTIVE,
            null,
            null
        );
        UUID printerAssetId = insertAsset(
            tenantId,
            "PRINT-TECH",
            "SN-PRINT",
            "mFp",
            AssetStatus.ACTIVE,
            null,
            null
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        mockMvc.perform(put("/api/v1/assets/{id}/technical-attributes", phoneAssetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("values", Map.of(
                    "phone_ip", "10.10.0.15",
                    "extension_number", "210",
                    "sip_line_id", "sip-line-210"
                )))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profile").value("STATIONARY_PHONE"))
            .andExpect(jsonPath("$.values.phone_ip").value("10.10.0.15"))
            .andExpect(jsonPath("$.values.extension_number").value("210"))
            .andExpect(jsonPath("$.values.sip_line_id").value("sip-line-210"));

        mockMvc.perform(put("/api/v1/assets/{id}/technical-attributes", printerAssetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("values", Map.of(
                    "printer_ip", "10.20.0.40",
                    "printer_network_name", "floor-2-mfp",
                    "printer_placement_note", "near reception"
                )))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profile").value("PRINTER"))
            .andExpect(jsonPath("$.values.printer_ip").value("10.20.0.40"))
            .andExpect(jsonPath("$.values.printer_network_name").value("floor-2-mfp"))
            .andExpect(jsonPath("$.values.printer_placement_note").value("near reception"));

        mockUserService.verify();
    }

    @Test
    void technicalAttributesRejectInvalidKeysProfilesAndValues() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID laptopAssetId = insertAsset(
            tenantId,
            "LAP-TECH",
            "SN-TECH",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null
        );
        UUID genericAssetId = insertAsset(
            tenantId,
            "ROUTER-TECH",
            "SN-ROUTER",
            "Router",
            AssetStatus.ACTIVE,
            null,
            null
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        mockMvc.perform(put("/api/v1/assets/{id}/technical-attributes", laptopAssetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("values", Map.of("unknown_key", "value")))))
            .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/v1/assets/{id}/technical-attributes", laptopAssetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("values", Map.of("phone_ip", "10.0.0.10")))))
            .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/v1/assets/{id}/technical-attributes", genericAssetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("values", Map.of("hostname", "router-01")))))
            .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/v1/assets/{id}/technical-attributes", laptopAssetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("values", Map.of("hostname", "h".repeat(513))))))
            .andExpect(status().isBadRequest());

        mockUserService.verify();
        assertThat(countRows("asset_technical_attributes")).isZero();
        assertThat(countRows("local_audit_records")).isZero();
    }

    @Test
    void technicalAttributesReplaceSemanticsNormalizeBlankAndSkipNoopAudit() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-TECH",
            "SN-TECH",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        mockMvc.perform(put("/api/v1/assets/{id}/technical-attributes", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("values", Map.of(
                    "hostname", "host-01",
                    "anydesk_id", "123456789"
                )))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.values.hostname").value("host-01"))
            .andExpect(jsonPath("$.values.anydesk_id").value("123456789"));

        Map<String, Object> replaceValues = new LinkedHashMap<>();
        replaceValues.put("hostname", "   ");
        replaceValues.put("ip_address", " 10.0.0.25 ");
        mockMvc.perform(put("/api/v1/assets/{id}/technical-attributes", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("values", replaceValues))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.values.hostname").doesNotExist())
            .andExpect(jsonPath("$.values.anydesk_id").doesNotExist())
            .andExpect(jsonPath("$.values.ip_address").value("10.0.0.25"));

        assertThat(technicalAttributeValue(tenantId, assetId, "hostname")).isNull();
        assertThat(technicalAttributeValue(tenantId, assetId, "anydesk_id")).isNull();
        assertThat(technicalAttributeValue(tenantId, assetId, "ip_address")).isEqualTo("10.0.0.25");
        assertThat(countRows("asset_technical_attributes")).isEqualTo(1);
        assertThat(countRows("local_audit_records")).isEqualTo(2);

        mockMvc.perform(put("/api/v1/assets/{id}/technical-attributes", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("values", Map.of(
                    "ip_address", "10.0.0.25"
                )))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.values.ip_address").value("10.0.0.25"));

        assertThat(countRows("local_audit_records")).isEqualTo(2);
        mockUserService.verify();
    }

    @Test
    void technicalAttributesUseExistingReadMutateRolesAndTenantIsolation() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantA,
            "LAP-TECH",
            "SN-TECH",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null
        );
        insertTechnicalAttribute(tenantA, assetId, "hostname", "host-01");
        UUID assetOperatorId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID tenantBAdminId = UUID.randomUUID();

        expectRoleLookup(tenantA, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectRoleLookup(tenantA, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantB, tenantBAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantB, tenantBAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.technical_attributes.values.hostname").value("host-01"));

        mockMvc.perform(put("/api/v1/assets/{id}/technical-attributes", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("values", Map.of("hostname", "host-02")))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.values.hostname").value("host-02"));

        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.technical_attributes.values.hostname").value("host-02"));

        mockMvc.perform(put("/api/v1/assets/{id}/technical-attributes", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "support-reviewer")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("values", Map.of("hostname", "host-03")))))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_USER_HEADER, tenantBAdminId))
            .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/v1/assets/{id}/technical-attributes", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_USER_HEADER, tenantBAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("values", Map.of("hostname", "tenant-b-host")))))
            .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/v1/assets/{id}/technical-attributes", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_ACTOR_HEADER, "missing-user")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("values", Map.of("hostname", "host-03")))))
            .andExpect(status().isBadRequest());

        assertThat(technicalAttributeValue(tenantA, assetId, "hostname")).isEqualTo("host-02");
        mockUserService.verify();
    }

    @Test
    void technicalAttributeSearchValidatesQueryLimitAndSupportedParameters() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .param("q", "   "))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .param("q", "h"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .param("q", "h".repeat(129)))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .param("q", "host")
                .param("limit", "0"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .param("q", "host")
                .param("limit", "21"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .param("q", "host")
                .param("keys", "hostname"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .param("q", "host")
                .param("limit", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.query").value("host"))
            .andExpect(jsonPath("$.limit").value(20))
            .andExpect(jsonPath("$.results").isEmpty());

        mockUserService.verify();
    }

    @Test
    void technicalAttributeSearchRanksDeduplicatesAndReturnsAssetOnlyResults() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID exactAssetId = insertAsset(
            tenantId,
            "A-EXACT",
            "SN-EXACT",
            "Laptop",
            AssetStatus.ACTIVE,
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Exact laptop",
            "Host note is not searched here"
        );
        UUID prefixAssetId = insertAsset(
            tenantId,
            "B-PREFIX",
            "SN-PREFIX",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null
        );
        UUID containsAssetId = insertAsset(
            tenantId,
            "C-CONTAINS",
            "SN-CONTAINS",
            "Laptop",
            AssetStatus.RETIRED,
            null,
            null
        );
        UUID duplicateMatchAssetId = insertAsset(
            tenantId,
            "D-DEDUP",
            "SN-DEDUP",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null
        );
        insertTechnicalAttribute(tenantId, exactAssetId, "hostname", "host");
        insertTechnicalAttribute(tenantId, prefixAssetId, "hostname", "Host-alpha");
        insertTechnicalAttribute(tenantId, containsAssetId, "hostname", "alpha-HOST");
        insertTechnicalAttribute(tenantId, duplicateMatchAssetId, "hostname", "same-host");
        insertTechnicalAttribute(tenantId, duplicateMatchAssetId, "ip_address", "same-host-ip");

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .param("q", "host"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.query").value("host"))
            .andExpect(jsonPath("$.limit").value(10))
            .andExpect(jsonPath("$.results.length()").value(4))
            .andExpect(jsonPath("$.results[0].asset_id").value(exactAssetId.toString()))
            .andExpect(jsonPath("$.results[0].asset_tag").value("A-EXACT"))
            .andExpect(jsonPath("$.results[0].display_name").value("Exact laptop"))
            .andExpect(jsonPath("$.results[0].asset_type").value("Laptop"))
            .andExpect(jsonPath("$.results[0].serial_number").value("SN-EXACT"))
            .andExpect(jsonPath("$.results[0].status").value(AssetStatus.ACTIVE))
            .andExpect(jsonPath("$.results[0].matched_field.key").value("hostname"))
            .andExpect(jsonPath("$.results[0].matched_field.label").value("Hostname"))
            .andExpect(jsonPath("$.results[0].matched_field.display_value").value("host"))
            .andExpect(jsonPath("$.results[0].matched_field.sensitive").value(false))
            .andExpect(jsonPath("$.results[0].matched_field.masked").value(false))
            .andExpect(jsonPath("$.results[1].asset_tag").value("B-PREFIX"))
            .andExpect(jsonPath("$.results[2].asset_tag").value("C-CONTAINS"))
            .andExpect(jsonPath("$.results[3].asset_tag").value("D-DEDUP"))
            .andExpect(jsonPath("$.results[3].matched_field.key").value("hostname"))
            .andExpect(jsonPath("$.results[0].technical_attributes").doesNotExist())
            .andExpect(jsonPath("$.results[0].assigned_user").doesNotExist())
            .andExpect(jsonPath("$.results[0].qr").doesNotExist())
            .andExpect(jsonPath("$.results[0].links").doesNotExist())
            .andExpect(jsonPath("$.users").doesNotExist())
            .andExpect(jsonPath("$.employees").doesNotExist());

        assertThat(countRows("local_audit_records")).isZero();
        mockUserService.verify();
    }

    @Test
    void technicalAttributeSearchCoversApprovedKeysAndMasksSensitiveValues() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();

        insertSearchAsset(tenantId, "ANYDESK", "Laptop", "anydesk_id", "123456789");
        insertSearchAsset(tenantId, "IP", "Laptop", "ip_address", "10.0.0.25");
        insertSearchAsset(tenantId, "MAC", "Laptop", "mac_address", "AA:BB:CC:DD:EE:FF");
        insertSearchAsset(tenantId, "PHONE-IP", "IP Phone", "phone_ip", "10.10.0.15");
        insertSearchAsset(tenantId, "EXT", "IP Phone", "extension_number", "210");
        insertSearchAsset(tenantId, "SIP", "IP Phone", "sip_line_id", "line-primary");
        insertSearchAsset(tenantId, "PRINTER-IP", "Printer", "printer_ip", "10.20.0.40");
        insertSearchAsset(tenantId, "PRINTER-NAME", "Printer", "printer_network_name", "floor-2-mfp");
        insertSearchAsset(tenantId, "PRINTER-PLACE", "Printer", "printer_placement_note", "placement-secret");

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        assertTechnicalAttributeSearchMatch(
            tenantId,
            tenantAdminId,
            "456789",
            "anydesk_id",
            "AnyDesk ID",
            "\u2022\u2022\u2022\u20226789",
            true,
            true
        );
        assertTechnicalAttributeSearchMatch(
            tenantId,
            tenantAdminId,
            "10.0.0",
            "ip_address",
            "IP-адрес",
            "10.0.0.xxx",
            true,
            true
        );
        assertTechnicalAttributeSearchMatch(
            tenantId,
            tenantAdminId,
            "bb:cc",
            "mac_address",
            "MAC-адрес",
            "AA:**:**:**:**:FF",
            true,
            true
        );
        assertTechnicalAttributeSearchMatch(
            tenantId,
            tenantAdminId,
            "10.10.0",
            "phone_ip",
            "IP телефона",
            "10.10.0.xxx",
            true,
            true
        );
        assertTechnicalAttributeSearchMatch(
            tenantId,
            tenantAdminId,
            "210",
            "extension_number",
            "Внутренний номер",
            "210",
            false,
            false
        );
        assertTechnicalAttributeSearchMatch(
            tenantId,
            tenantAdminId,
            "primary",
            "sip_line_id",
            "SIP / линия",
            "lin\u2026ry",
            true,
            true
        );
        assertTechnicalAttributeSearchMatch(
            tenantId,
            tenantAdminId,
            "10.20.0",
            "printer_ip",
            "IP принтера",
            "10.20.0.xxx",
            true,
            true
        );
        assertTechnicalAttributeSearchMatch(
            tenantId,
            tenantAdminId,
            "floor",
            "printer_network_name",
            "Сетевое имя принтера",
            "floor-2-mfp",
            false,
            false
        );
        assertTechnicalAttributeSearchMatch(
            tenantId,
            tenantAdminId,
            "placement-secret",
            "printer_placement_note",
            "Примечание по размещению",
            "\u2022\u2022\u2022\u2022",
            true,
            true
        );

        mockUserService.verify();
    }

    @Test
    void technicalAttributeSearchExcludesUnapprovedDataAndKeepsTenantIsolation() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID tenantAAdminId = UUID.randomUUID();
        UUID tenantBAdminId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantA,
            "QR-SECRET",
            "SN-SECRET",
            "Printer",
            AssetStatus.ACTIVE,
            null,
            null,
            "Searchable display name is not searched here",
            "note-secret"
        );
        UUID tenantBAssetId = insertAsset(
            tenantB,
            "TENANT-B",
            "SN-B",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null
        );
        insertTechnicalAttribute(tenantA, assetId, "printer_placement_note", "placement-secret");
        insertTechnicalAttribute(tenantA, assetId, "unsupported_custom", "custom-secret");
        insertTechnicalAttribute(tenantB, tenantBAssetId, "hostname", "tenant-b-host");
        insertAssetAuditRecord(
            tenantA,
            assetId,
            com.itam.assetservice.domain.AuditAction.UPDATE,
            "tenant-admin",
            "corr-audit-secret",
            "{\"audit\":\"audit-secret\"}",
            java.time.Instant.now()
        );

        expectRoleLookup(tenantA, tenantAAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantA, tenantAAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantA, tenantAAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantA, tenantAAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantA, tenantAAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantB, tenantBAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, tenantAAdminId)
                .param("q", "placement-secret"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results.length()").value(1))
            .andExpect(jsonPath("$.results[0].asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.results[0].matched_field.key").value("printer_placement_note"))
            .andExpect(jsonPath("$.results[0].matched_field.label").value("Примечание по размещению"))
            .andExpect(jsonPath("$.results[0].matched_field.display_value").isNotEmpty())
            .andExpect(jsonPath("$.results[0].matched_field.display_value").value("\u2022\u2022\u2022\u2022"))
            .andExpect(jsonPath("$.results[0].matched_field.display_value").value(org.hamcrest.Matchers.not("placement-secret")))
            .andExpect(jsonPath("$.results[0].matched_field.sensitive").value(true))
            .andExpect(jsonPath("$.results[0].matched_field.masked").value(true))
            .andExpect(jsonPath("$.results[0].technical_attributes").doesNotExist());
        assertTechnicalAttributeSearchEmpty(tenantA, tenantAAdminId, "custom-secret");
        assertTechnicalAttributeSearchEmpty(tenantA, tenantAAdminId, "note-secret");
        assertTechnicalAttributeSearchEmpty(tenantA, tenantAAdminId, "audit-secret");
        assertTechnicalAttributeSearchEmpty(tenantA, tenantAAdminId, "QR-SECRET");

        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_USER_HEADER, tenantBAdminId)
                .param("q", "tenant-b-host"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results.length()").value(1))
            .andExpect(jsonPath("$.results[0].asset_id").value(tenantBAssetId.toString()));

        mockUserService.verify();
    }

    @Test
    void technicalAttributeSearchUsesExistingReadRolesAndDoesNotEmitAudit() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-SEARCH",
            "SN-SEARCH",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null
        );
        insertTechnicalAttribute(tenantId, assetId, "hostname", "role-host");
        UUID tenantAdminId = UUID.randomUUID();
        UUID assetOperatorId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID callerWithoutRoleId = UUID.randomUUID();

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantId, callerWithoutRoleId, List.of());

        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .param("q", "role-host"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results[0].asset_id").value(assetId.toString()));

        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .param("q", "role-host"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results[0].asset_id").value(assetId.toString()));

        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .param("q", "role-host"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results[0].asset_id").value(assetId.toString()));

        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerWithoutRoleId)
                .param("q", "role-host"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .param("q", "role-host"))
            .andExpect(status().isBadRequest());

        assertThat(countRows("local_audit_records")).isZero();
        mockUserService.verify();
    }

    @Test
    void employeeSheetTechnicalHintsValidateParamsAndUseExistingReadRoles() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assignedUserId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assetOperatorId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID callerWithoutRoleId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "HINT-ROLE",
            "SN-HINT-ROLE",
            "Laptop",
            AssetStatus.ACTIVE,
            assignedUserId,
            null
        );
        insertTechnicalAttribute(tenantId, assetId, "hostname", "role-host");

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantId, callerWithoutRoleId, List.of());

        mockMvc.perform(get("/api/v1/assets/employee-sheet-technical-hints")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/assets/employee-sheet-technical-hints")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .param("user_id", "   "))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/assets/employee-sheet-technical-hints")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .param("user_id", "not-a-uuid"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/assets/employee-sheet-technical-hints")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .param("user_id", assignedUserId.toString())
                .param("include_sensitive", "true"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/assets/employee-sheet-technical-hints")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .param("user_id", assignedUserId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assets[0].asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.assets[0].technical_hints.hostname").value("role-host"));

        mockMvc.perform(get("/api/v1/assets/employee-sheet-technical-hints")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .param("user_id", assignedUserId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assets[0].asset_id").value(assetId.toString()));

        mockMvc.perform(get("/api/v1/assets/employee-sheet-technical-hints")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .param("user_id", assignedUserId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assets[0].asset_id").value(assetId.toString()));

        mockMvc.perform(get("/api/v1/assets/employee-sheet-technical-hints")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerWithoutRoleId)
                .param("user_id", assignedUserId.toString()))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/assets/employee-sheet-technical-hints")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .param("user_id", assignedUserId.toString()))
            .andExpect(status().isBadRequest());

        assertThat(countRows("local_audit_records")).isZero();
        mockUserService.verify();
    }

    @Test
    void employeeSheetTechnicalHintsReturnAssignedAssetsWithOnlyLowSensitivityHints() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID selectedUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID tenantAAdminId = UUID.randomUUID();
        UUID tenantBAdminId = UUID.randomUUID();
        UUID laptopLocationId = UUID.randomUUID();
        UUID phoneLocationId = UUID.randomUUID();
        UUID printerLocationId = UUID.randomUUID();
        UUID laptopAssetId = insertAsset(
            tenantA,
            "A-LAPTOP",
            "SN-LAPTOP",
            "Laptop",
            AssetStatus.ACTIVE,
            selectedUserId,
            laptopLocationId,
            "Assigned laptop",
            null
        );
        UUID phoneAssetId = insertAsset(
            tenantA,
            "B-PHONE",
            "SN-PHONE",
            "Stationary phone",
            AssetStatus.ACTIVE,
            selectedUserId,
            phoneLocationId,
            "Assigned phone",
            null
        );
        UUID printerAssetId = insertAsset(
            tenantA,
            "C-PRINTER",
            "SN-PRINTER",
            "Printer",
            AssetStatus.RETIRED,
            selectedUserId,
            printerLocationId,
            "Assigned printer",
            null
        );
        UUID genericAssetId = insertAsset(
            tenantA,
            "D-GENERIC",
            "SN-GENERIC",
            "Projector",
            AssetStatus.ACTIVE,
            selectedUserId,
            null,
            "Generic assigned",
            null
        );
        UUID otherUserAssetId = insertAsset(
            tenantA,
            "E-OTHER-USER",
            "SN-OTHER-USER",
            "Laptop",
            AssetStatus.ACTIVE,
            otherUserId,
            null
        );
        insertAsset(
            tenantA,
            "F-UNASSIGNED",
            "SN-UNASSIGNED",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null
        );
        UUID tenantBAssetId = insertAsset(
            tenantB,
            "TENANT-B-LAPTOP",
            "SN-TENANT-B",
            "Laptop",
            AssetStatus.ACTIVE,
            selectedUserId,
            null
        );

        insertTechnicalAttribute(tenantA, laptopAssetId, "hostname", "  laptop-host  ");
        insertTechnicalAttribute(tenantA, laptopAssetId, "anydesk_id", "123456789");
        insertTechnicalAttribute(tenantA, laptopAssetId, "ip_address", "10.0.0.25");
        insertTechnicalAttribute(tenantA, laptopAssetId, "mac_address", "AA:BB:CC:DD:EE:FF");
        insertTechnicalAttribute(tenantA, phoneAssetId, "extension_number", "214");
        insertTechnicalAttribute(tenantA, phoneAssetId, "phone_ip", "10.10.0.15");
        insertTechnicalAttribute(tenantA, phoneAssetId, "sip_line_id", "line-primary");
        insertTechnicalAttribute(tenantA, printerAssetId, "printer_network_name", "printer-main");
        insertTechnicalAttribute(tenantA, printerAssetId, "printer_ip", "10.20.0.40");
        insertTechnicalAttribute(tenantA, printerAssetId, "printer_placement_note", "floor-secret");
        insertTechnicalAttribute(tenantA, genericAssetId, "ip_address", "10.30.0.50");
        insertTechnicalAttribute(tenantA, otherUserAssetId, "hostname", "other-user-host");
        insertTechnicalAttribute(tenantB, tenantBAssetId, "hostname", "tenant-b-host");

        expectRoleLookup(tenantA, tenantAAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantA, tenantAAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantA, tenantAAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantB, tenantBAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        mockMvc.perform(get("/api/v1/assets/employee-sheet-technical-hints")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, tenantAAdminId)
                .param("user_id", selectedUserId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user_id").value(selectedUserId.toString()))
            .andExpect(jsonPath("$.assets.length()").value(4))
            .andExpect(jsonPath("$.assets[0].asset_id").value(laptopAssetId.toString()))
            .andExpect(jsonPath("$.assets[0].asset_tag").value("A-LAPTOP"))
            .andExpect(jsonPath("$.assets[0].display_name").value("Assigned laptop"))
            .andExpect(jsonPath("$.assets[0].asset_type").value("Laptop"))
            .andExpect(jsonPath("$.assets[0].serial_number").value("SN-LAPTOP"))
            .andExpect(jsonPath("$.assets[0].status").value(AssetStatus.ACTIVE))
            .andExpect(jsonPath("$.assets[0].location_id").value(laptopLocationId.toString()))
            .andExpect(jsonPath("$.assets[0].technical_hints.hostname").value("laptop-host"))
            .andExpect(jsonPath("$.assets[0].technical_hints.anydesk_id").doesNotExist())
            .andExpect(jsonPath("$.assets[0].technical_hints.ip_address").doesNotExist())
            .andExpect(jsonPath("$.assets[0].technical_hints.mac_address").doesNotExist())
            .andExpect(jsonPath("$.assets[0].technical_attributes").doesNotExist())
            .andExpect(jsonPath("$.assets[0].profile").doesNotExist())
            .andExpect(jsonPath("$.assets[0].note").doesNotExist())
            .andExpect(jsonPath("$.assets[0].qr").doesNotExist())
            .andExpect(jsonPath("$.assets[0].audit").doesNotExist())
            .andExpect(jsonPath("$.assets[0].links").doesNotExist())
            .andExpect(jsonPath("$.assets[1].asset_id").value(phoneAssetId.toString()))
            .andExpect(jsonPath("$.assets[1].technical_hints.extension_number").value("214"))
            .andExpect(jsonPath("$.assets[1].technical_hints.phone_ip").doesNotExist())
            .andExpect(jsonPath("$.assets[1].technical_hints.sip_line_id").doesNotExist())
            .andExpect(jsonPath("$.assets[2].asset_id").value(printerAssetId.toString()))
            .andExpect(jsonPath("$.assets[2].status").value(AssetStatus.RETIRED))
            .andExpect(jsonPath("$.assets[2].technical_hints.printer_network_name").value("printer-main"))
            .andExpect(jsonPath("$.assets[2].technical_hints.printer_ip").doesNotExist())
            .andExpect(jsonPath("$.assets[2].technical_hints.printer_placement_note").doesNotExist())
            .andExpect(jsonPath("$.assets[3].asset_id").value(genericAssetId.toString()))
            .andExpect(jsonPath("$.assets[3].technical_hints").isEmpty());

        mockMvc.perform(get("/api/v1/assets/employee-sheet-technical-hints")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, tenantAAdminId)
                .param("user_id", otherUserId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assets.length()").value(1))
            .andExpect(jsonPath("$.assets[0].asset_id").value(otherUserAssetId.toString()))
            .andExpect(jsonPath("$.assets[0].technical_hints.hostname").value("other-user-host"));

        mockMvc.perform(get("/api/v1/assets/employee-sheet-technical-hints")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, tenantAAdminId)
                .param("user_id", UUID.randomUUID().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assets").isEmpty());

        mockMvc.perform(get("/api/v1/assets/employee-sheet-technical-hints")
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_USER_HEADER, tenantBAdminId)
                .param("user_id", selectedUserId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assets.length()").value(1))
            .andExpect(jsonPath("$.assets[0].asset_id").value(tenantBAssetId.toString()))
            .andExpect(jsonPath("$.assets[0].technical_hints.hostname").value("tenant-b-host"));

        assertThat(countRows("local_audit_records")).isZero();
        mockUserService.verify();
    }

    @Test
    void employeeSheetTechnicalHintsDoNotChangeExistingAssetEndpoints() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assignedUserId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "HINT-BOUNDARY",
            "SN-HINT-BOUNDARY",
            "Laptop",
            AssetStatus.ACTIVE,
            assignedUserId,
            null
        );
        insertTechnicalAttribute(tenantId, assetId, "hostname", "boundary-host");

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        mockMvc.perform(get("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].technical_hints").doesNotExist())
            .andExpect(jsonPath("$[0].technical_attributes").doesNotExist());

        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.technical_attributes.values.hostname").value("boundary-host"))
            .andExpect(jsonPath("$.technical_hints").doesNotExist());

        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .param("q", "boundary-host"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results[0].matched_field.key").value("hostname"))
            .andExpect(jsonPath("$.results[0].technical_hints").doesNotExist());

        mockMvc.perform(get("/api/v1/assets/employee-sheet-technical-hints")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .param("user_id", assignedUserId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assets[0].technical_hints.hostname").value("boundary-host"))
            .andExpect(jsonPath("$.assets[0].technical_attributes").doesNotExist());

        assertThat(countRows("local_audit_records")).isZero();
        mockUserService.verify();
    }

    @Test
    void genericPatchDoesNotUpdateTechnicalAttributes() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-TECH",
            "SN-TECH",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null
        );
        insertTechnicalAttribute(tenantId, assetId, "hostname", "host-01");

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        Map<String, Object> patchPayload = assetPayload("LAP-TECH-UPDATED", "SN-TECH", "Laptop");
        patchPayload.put("technical_attributes", Map.of("values", Map.of("hostname", "host-02")));
        mockMvc.perform(patch("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchPayload)))
            .andExpect(status().isBadRequest());

        assertThat(technicalAttributeValue(tenantId, assetId, "hostname")).isEqualTo("host-01");
        assertThat(countRows("asset_technical_attributes")).isEqualTo(1);
        assertThat(assetRow(tenantId, assetId).assetTag()).isEqualTo("LAP-TECH");
        assertThat(countRows("local_audit_records")).isEqualTo(0);
        mockUserService.verify();
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
            AssetStatus.ACTIVE,
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
                    "asset_type", "Laptop Pro"
                ))))
            .andExpect(status().isNotFound());

        mockUserService.verify();
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    private UUID insertSearchAsset(UUID tenantId, String assetTagSuffix, String assetType, String key, String value) {
        UUID assetId = insertAsset(
            tenantId,
            "SEARCH-" + assetTagSuffix,
            "SN-" + assetTagSuffix,
            assetType,
            AssetStatus.ACTIVE,
            null,
            null
        );
        insertTechnicalAttribute(tenantId, assetId, key, value);
        return assetId;
    }

    private void assertTechnicalAttributeSearchMatch(
        UUID tenantId,
        UUID authenticatedUserId,
        String query,
        String expectedKey,
        String expectedLabel,
        String expectedDisplayValue,
        boolean expectedSensitive,
        boolean expectedMasked
    ) throws Exception {
        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, authenticatedUserId)
                .param("q", query)
                .param("limit", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results.length()").value(1))
            .andExpect(jsonPath("$.results[0].matched_field.key").value(expectedKey))
            .andExpect(jsonPath("$.results[0].matched_field.label").value(expectedLabel))
            .andExpect(jsonPath("$.results[0].matched_field.display_value").value(expectedDisplayValue))
            .andExpect(jsonPath("$.results[0].matched_field.sensitive").value(expectedSensitive))
            .andExpect(jsonPath("$.results[0].matched_field.masked").value(expectedMasked))
            .andExpect(jsonPath("$.results[0].technical_attributes").doesNotExist());
    }

    private void assertTechnicalAttributeSearchEmpty(
        UUID tenantId,
        UUID authenticatedUserId,
        String query
    ) throws Exception {
        mockMvc.perform(get("/api/v1/assets/technical-attribute-search")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, authenticatedUserId)
                .param("q", query))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results").isEmpty());
    }

    private Map<String, Object> assetPayload(String assetTag, String serialNumber, String assetType) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("asset_tag", assetTag);
        payload.put("serial_number", serialNumber);
        payload.put("asset_type", assetType);
        return payload;
    }
}
