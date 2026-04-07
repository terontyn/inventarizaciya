package com.itam.userservice;

import static com.itam.userservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_ACTOR_HEADER;
import static com.itam.userservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.userservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static com.itam.userservice.api.AuthenticatedRequestContextExtractor.CORRELATION_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.itam.userservice.application.PilotRoleNames;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PilotRbacApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void tenantAdminCanBindRoleToUserAndDuplicateBindingConflicts() throws Exception {
        UUID tenantId = insertTenant("Tenant A");
        UUID tenantAdminId = insertTenantAdmin(tenantId, "Tenant Admin");
        UUID targetUserId = insertUser(tenantId, "Operator User", null);
        UUID roleId = insertRole(tenantId, PilotRoleNames.ASSET_OPERATOR);

        mockMvc.perform(post("/api/v1/users/{userId}/roles", targetUserId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-bind-role")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("role_id", roleId))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.user_id").value(targetUserId.toString()))
            .andExpect(jsonPath("$.role_id").value(roleId.toString()));

        Integer bindingCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user_roles WHERE tenant_id = ? AND user_id = ? AND role_id = ?",
            Integer.class,
            tenantId,
            targetUserId,
            roleId
        );
        assertThat(bindingCount).isEqualTo(1);

        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.entityType()).isEqualTo("USER_ROLE");
        assertThat(auditRow.actionType()).isEqualTo("CREATE");
        assertThat(auditRow.correlationId()).isEqualTo("corr-bind-role");

        mockMvc.perform(post("/api/v1/users/{userId}/roles", targetUserId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("role_id", roleId))))
            .andExpect(status().isConflict());
    }

    @Test
    void crossTenantUserRoleBindingIsRejected() throws Exception {
        UUID tenantA = insertTenant("Tenant A");
        UUID tenantB = insertTenant("Tenant B");
        UUID tenantAdminId = insertTenantAdmin(tenantA, "Tenant Admin");
        UUID targetUserId = insertUser(tenantA, "Operator User", null);
        UUID foreignRoleId = insertRole(tenantB, PilotRoleNames.ASSET_OPERATOR);

        mockMvc.perform(post("/api/v1/users/{userId}/roles", targetUserId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("role_id", foreignRoleId))))
            .andExpect(status().isBadRequest());

        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void nonAdminCannotBindRoleToUser() throws Exception {
        UUID tenantId = insertTenant("Tenant A");
        UUID assetOperatorId = insertAssetOperator(tenantId, "Asset Operator");
        UUID targetUserId = insertUser(tenantId, "Support User", null);
        UUID roleId = insertRole(tenantId, PilotRoleNames.SUPPORT_REVIEWER);

        mockMvc.perform(post("/api/v1/users/{userId}/roles", targetUserId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("role_id", roleId))))
            .andExpect(status().isForbidden());
    }

    @Test
    void assetOperatorCanReadReferenceDataButCannotMutateIt() throws Exception {
        UUID tenantId = insertTenant("Tenant A");
        UUID assetOperatorId = insertAssetOperator(tenantId, "Asset Operator");
        UUID departmentId = insertDepartment(tenantId, "Finance");
        UUID branchId = insertBranch(tenantId, "HQ");
        UUID locationId = insertLocation(tenantId, branchId, "Room 101");
        UUID userId = insertUser(tenantId, "Alice", departmentId);
        UUID roleId = insertRole(tenantId, PilotRoleNames.SUPPORT_REVIEWER);

        mockMvc.perform(get("/api/v1/users")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(userId.toString()));

        mockMvc.perform(get("/api/v1/departments")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(departmentId.toString()));

        mockMvc.perform(get("/api/v1/branches")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(branchId.toString()));

        mockMvc.perform(get("/api/v1/locations")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(locationId.toString()));

        mockMvc.perform(get("/api/v1/roles")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/users")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Bob"))))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/departments")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "IT"))))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/branches")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Remote"))))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/locations")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Room 102",
                    "branch_id", branchId
                ))))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/roles")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Spare Role"))))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/users/{userId}/roles", userId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("role_id", roleId))))
            .andExpect(status().isForbidden());
    }

    @Test
    void supportReviewerCanReadAllowedReferenceDataButCannotMutateAnythingOrReadRoles() throws Exception {
        UUID tenantId = insertTenant("Tenant A");
        UUID supportReviewerId = insertSupportReviewer(tenantId, "Support Reviewer");
        UUID departmentId = insertDepartment(tenantId, "Finance");
        UUID branchId = insertBranch(tenantId, "HQ");
        UUID locationId = insertLocation(tenantId, branchId, "Room 101");
        UUID userId = insertUser(tenantId, "Alice", departmentId);
        UUID targetRoleId = insertRole(tenantId, PilotRoleNames.ASSET_OPERATOR);

        mockMvc.perform(get("/api/v1/users")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(userId.toString()));

        mockMvc.perform(get("/api/v1/departments")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(departmentId.toString()));

        mockMvc.perform(get("/api/v1/branches")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(branchId.toString()));

        mockMvc.perform(get("/api/v1/locations")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(locationId.toString()));

        mockMvc.perform(get("/api/v1/roles")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/users")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "support-reviewer")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Bob"))))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/departments")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "support-reviewer")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "IT"))))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/users/{userId}/roles", userId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "support-reviewer")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("role_id", targetRoleId))))
            .andExpect(status().isForbidden());
    }

    @Test
    void roleEnforcementIsTenantScoped() throws Exception {
        UUID tenantA = insertTenant("Tenant A");
        UUID tenantB = insertTenant("Tenant B");
        UUID tenantAAdminId = insertTenantAdmin(tenantA, "Tenant A Admin");
        insertTenantAdmin(tenantB, "Tenant B Admin");

        mockMvc.perform(get("/api/v1/users")
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_USER_HEADER, tenantAAdminId))
            .andExpect(status().isForbidden());
    }

    @Test
    void internalRoleLookupReturnsRoleNamesInTenantScope() throws Exception {
        UUID tenantA = insertTenant("Tenant A");
        UUID tenantB = insertTenant("Tenant B");
        UUID assetOperatorId = insertAssetOperator(tenantA, "Asset Operator");

        mockMvc.perform(get("/internal/api/v1/users/{userId}/roles", assetOperatorId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user_id").value(assetOperatorId.toString()))
            .andExpect(jsonPath("$.role_names[0]").value(PilotRoleNames.ASSET_OPERATOR));

        mockMvc.perform(get("/internal/api/v1/users/{userId}/roles", assetOperatorId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user_id").value(assetOperatorId.toString()))
            .andExpect(jsonPath("$.role_names").isEmpty());
    }
}
