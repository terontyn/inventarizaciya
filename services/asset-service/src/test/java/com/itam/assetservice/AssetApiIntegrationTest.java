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
import com.itam.assetservice.domain.AssetStatus;
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
}
