package com.itam.softwareservice;

import static com.itam.softwareservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_ACTOR_HEADER;
import static com.itam.softwareservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.softwareservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static com.itam.softwareservice.api.AuthenticatedRequestContextExtractor.CORRELATION_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.itam.softwareservice.application.PilotRoleNames;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class SoftwareApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void readEndpointsReturnTenantScopedCurrentStateVisibility() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID userA = UUID.randomUUID();
        UUID assetA = UUID.randomUUID();
        UUID recordA1 = insertSoftwareRecord(tenantA, "Office");
        UUID recordA2 = insertSoftwareRecord(tenantA, "VPN Client");
        insertSoftwareRecord(tenantB, "Other Tenant App");

        insertSoftwareUserAllocation(tenantA, recordA1, userA);
        insertSoftwareUserAllocation(tenantA, recordA2, userA);
        insertSoftwareAssetAllocation(tenantA, recordA1, assetA);
        insertSoftwareAssetAllocation(tenantB, insertSoftwareRecord(tenantB, "B Asset App"), UUID.randomUUID());

        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectUserLookup(tenantA, supportReviewerId, userA, HttpStatus.OK);
        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectAssetLookup(tenantA, supportReviewerId, assetA, HttpStatus.OK);

        mockMvc.perform(get("/api/v1/software/records")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/v1/software/records/{id}", recordA1)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(recordA1.toString()))
            .andExpect(jsonPath("$.name").value("Office"))
            .andExpect(jsonPath("$.allocated_user_ids[0]").value(userA.toString()))
            .andExpect(jsonPath("$.allocated_asset_ids[0]").value(assetA.toString()));

        mockMvc.perform(get("/api/v1/software/users/{userId}/software", userA)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/v1/software/assets/{assetId}/software", assetA)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(recordA1.toString()));

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void assetOperatorCanCreateAllocateAndRemoveWithAudit() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetOperatorId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectUserLookup(tenantId, assetOperatorId, userId, HttpStatus.OK);
        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, assetOperatorId, assetId, HttpStatus.OK);
        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectUserLookup(tenantId, assetOperatorId, userId, HttpStatus.OK);
        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, assetOperatorId, assetId, HttpStatus.OK);
        String createResponse = mockMvc.perform(post("/api/v1/software/records")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .header(CORRELATION_ID_HEADER, "corr-create")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Remote Agent"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Remote Agent"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        UUID recordId = UUID.fromString(objectMapper.readTree(createResponse).path("id").asText());
        AuditRow createAudit = latestAuditRow();
        assertThat(createAudit.actionType()).isEqualTo("CREATE_SOFTWARE_RECORD");
        assertThat(createAudit.correlationId()).isEqualTo("corr-create");
        assertThat(createAudit.changePayload()).contains("Remote Agent");

        mockMvc.perform(post("/api/v1/software/records/{id}/allocate-to-user", recordId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .header(CORRELATION_ID_HEADER, "corr-alloc-user")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("user_id", userId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.allocated_user_ids[0]").value(userId.toString()));

        AuditRow allocUserAudit = latestAuditRow();
        assertThat(allocUserAudit.actionType()).isEqualTo("ALLOCATE_TO_USER");
        assertThat(allocUserAudit.correlationId()).isEqualTo("corr-alloc-user");

        mockMvc.perform(post("/api/v1/software/records/{id}/allocate-to-asset", recordId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .header(CORRELATION_ID_HEADER, "corr-alloc-asset")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("asset_id", assetId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.allocated_asset_ids[0]").value(assetId.toString()));

        AuditRow allocAssetAudit = latestAuditRow();
        assertThat(allocAssetAudit.actionType()).isEqualTo("ALLOCATE_TO_ASSET");
        assertThat(allocAssetAudit.correlationId()).isEqualTo("corr-alloc-asset");

        mockMvc.perform(post("/api/v1/software/records/{id}/remove-from-user", recordId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .header(CORRELATION_ID_HEADER, "corr-remove-user")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("user_id", userId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.allocated_user_ids.length()").value(0));

        AuditRow removeUserAudit = latestAuditRow();
        assertThat(removeUserAudit.actionType()).isEqualTo("REMOVE_FROM_USER");
        assertThat(removeUserAudit.correlationId()).isEqualTo("corr-remove-user");

        mockMvc.perform(post("/api/v1/software/records/{id}/remove-from-asset", recordId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .header(CORRELATION_ID_HEADER, "corr-remove-asset")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("asset_id", assetId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.allocated_asset_ids.length()").value(0));

        AuditRow removeAssetAudit = latestAuditRow();
        assertThat(removeAssetAudit.actionType()).isEqualTo("REMOVE_FROM_ASSET");
        assertThat(removeAssetAudit.correlationId()).isEqualTo("corr-remove-asset");

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void supportReviewerCanReadButCannotWrite() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID recordId = insertSoftwareRecord(tenantId, "AnyDesk");
        UUID userId = UUID.randomUUID();

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));

        mockMvc.perform(get("/api/v1/software/records")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/software/records/{id}/allocate-to-user", recordId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "support-reviewer")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("user_id", userId))))
            .andExpect(status().isForbidden());

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void duplicateCurrentRelationsAreRejected() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID recordId = insertSoftwareRecord(tenantId, "Office");
        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        insertSoftwareUserAllocation(tenantId, recordId, userId);
        insertSoftwareAssetAllocation(tenantId, recordId, assetId);

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectUserLookup(tenantId, tenantAdminId, userId, HttpStatus.OK);
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectAssetLookup(tenantId, tenantAdminId, assetId, HttpStatus.OK);

        mockMvc.perform(post("/api/v1/software/records/{id}/allocate-to-user", recordId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("user_id", userId))))
            .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/software/records/{id}/allocate-to-asset", recordId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("asset_id", assetId))))
            .andExpect(status().isConflict());

        mockUserService.verify();
        mockAssetService.verify();
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void removalRejectsMissingCurrentRelation() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID recordId = insertSoftwareRecord(tenantId, "VPN");
        UUID userId = UUID.randomUUID();

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectUserLookup(tenantId, tenantAdminId, userId, HttpStatus.OK);

        mockMvc.perform(post("/api/v1/software/records/{id}/remove-from-user", recordId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("user_id", userId))))
            .andExpect(status().isNotFound());

        mockUserService.verify();
        mockAssetService.verify();
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void allocationRejectsUnknownTargetReferences() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID recordId = insertSoftwareRecord(tenantId, "Endpoint Agent");
        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectUserLookup(tenantId, tenantAdminId, userId, HttpStatus.NOT_FOUND);
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectAssetLookup(tenantId, tenantAdminId, assetId, HttpStatus.NOT_FOUND);

        mockMvc.perform(post("/api/v1/software/records/{id}/allocate-to-user", recordId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("user_id", userId))))
            .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/software/records/{id}/allocate-to-asset", recordId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("asset_id", assetId))))
            .andExpect(status().isNotFound());

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void endpointsReturnServiceUnavailableWhenCrossServiceLookupFails() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID callerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID recordId = insertSoftwareRecord(tenantId, "Remote Help");

        expectRoleLookupFailure(tenantId, callerId, HttpStatus.INTERNAL_SERVER_ERROR);
        expectRoleLookup(tenantId, callerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectUserLookup(tenantId, callerId, userId, HttpStatus.INTERNAL_SERVER_ERROR);
        expectRoleLookup(tenantId, callerId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, callerId, assetId, HttpStatus.INTERNAL_SERVER_ERROR);

        mockMvc.perform(get("/api/v1/software/records")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId))
            .andExpect(status().isServiceUnavailable());

        mockMvc.perform(get("/api/v1/software/users/{userId}/software", userId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId))
            .andExpect(status().isServiceUnavailable());

        mockMvc.perform(post("/api/v1/software/records/{id}/allocate-to-asset", recordId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("asset_id", assetId))))
            .andExpect(status().isServiceUnavailable());

        mockUserService.verify();
        mockAssetService.verify();
    }
}
