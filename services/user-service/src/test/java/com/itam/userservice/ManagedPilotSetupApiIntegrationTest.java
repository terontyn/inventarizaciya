package com.itam.userservice;

import static com.itam.userservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_ACTOR_HEADER;
import static com.itam.userservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.userservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static com.itam.userservice.api.AuthenticatedRequestContextExtractor.CORRELATION_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.itam.userservice.application.PilotRoleNames;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ManagedPilotSetupApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void bootstrapTenantAdminCanCompleteManagedPilotSetupPathWithExistingApis() throws Exception {
        UUID tenantId = insertTenant("Tenant A");
        UUID bootstrapTenantAdminId = insertTenantAdmin(tenantId, "Bootstrap Tenant Admin");

        UUID assetOperatorRoleId = createRole(tenantId, bootstrapTenantAdminId, PilotRoleNames.ASSET_OPERATOR);
        UUID supportReviewerRoleId = createRole(tenantId, bootstrapTenantAdminId, PilotRoleNames.SUPPORT_REVIEWER);

        UUID assetOperatorUserId = createUser(tenantId, bootstrapTenantAdminId, "Alice Operator", null);
        bindRole(tenantId, bootstrapTenantAdminId, assetOperatorUserId, assetOperatorRoleId, "corr-bind-operator");

        UUID supportReviewerUserId = createUser(tenantId, bootstrapTenantAdminId, "Sam Reviewer", null);
        bindRole(tenantId, bootstrapTenantAdminId, supportReviewerUserId, supportReviewerRoleId, "corr-bind-reviewer");

        UUID branchId = createBranch(tenantId, bootstrapTenantAdminId, "HQ");
        UUID locationId = createLocation(tenantId, bootstrapTenantAdminId, branchId, "Room 101");
        UUID departmentId = createDepartment(tenantId, bootstrapTenantAdminId, "Finance");

        mockMvc.perform(patch("/api/v1/users/{id}", assetOperatorUserId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, bootstrapTenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-operator-user-update")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Alice Operator",
                    "department_id", departmentId
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetOperatorUserId.toString()))
            .andExpect(jsonPath("$.department_id").value(departmentId.toString()));

        mockMvc.perform(get("/api/v1/users/{id}", assetOperatorUserId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, bootstrapTenantAdminId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetOperatorUserId.toString()))
            .andExpect(jsonPath("$.department_id").value(departmentId.toString()));

        mockMvc.perform(get("/api/v1/locations/{id}", locationId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, bootstrapTenantAdminId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(locationId.toString()))
            .andExpect(jsonPath("$.branch_id").value(branchId.toString()));

        assertThat(countRows("roles")).isEqualTo(3);
        assertThat(countRows("users")).isEqualTo(3);
        assertThat(countRows("user_roles")).isEqualTo(3);
        assertThat(countRows("branches")).isEqualTo(1);
        assertThat(countRows("locations")).isEqualTo(1);
        assertThat(countRows("departments")).isEqualTo(1);
        assertThat(countRows("local_audit_records")).isEqualTo(10);

        AuditRow latestAudit = latestAuditRow();
        assertThat(latestAudit.entityType()).isEqualTo("USER");
        assertThat(latestAudit.actionType()).isEqualTo("UPDATE");
        assertThat(latestAudit.correlationId()).isEqualTo("corr-operator-user-update");
    }

    private UUID createRole(UUID tenantId, UUID authenticatedUserId, String roleName) throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/roles")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, authenticatedUserId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-role-" + roleName.replace(" ", "-").toLowerCase())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", roleName))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(roleName))
            .andReturn()
            .getResponse()
            .getContentAsString();
        return extractId(responseBody);
    }

    private UUID createUser(
        UUID tenantId,
        UUID authenticatedUserId,
        String userName,
        UUID departmentId
    ) throws Exception {
        Map<String, Object> requestBody = departmentId == null
            ? Map.of("name", userName)
            : Map.of("name", userName, "department_id", departmentId);
        String responseBody = mockMvc.perform(post("/api/v1/users")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, authenticatedUserId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-user-" + userName.replace(" ", "-").toLowerCase())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(userName))
            .andReturn()
            .getResponse()
            .getContentAsString();
        return extractId(responseBody);
    }

    private void bindRole(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID userId,
        UUID roleId,
        String correlationId
    ) throws Exception {
        mockMvc.perform(post("/api/v1/users/{userId}/roles", userId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, authenticatedUserId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, correlationId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("role_id", roleId))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.user_id").value(userId.toString()))
            .andExpect(jsonPath("$.role_id").value(roleId.toString()));
    }

    private UUID createBranch(UUID tenantId, UUID authenticatedUserId, String branchName) throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/branches")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, authenticatedUserId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-branch-create")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", branchName))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(branchName))
            .andReturn()
            .getResponse()
            .getContentAsString();
        return extractId(responseBody);
    }

    private UUID createLocation(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID branchId,
        String locationName
    ) throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/locations")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, authenticatedUserId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-location-create")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", locationName,
                    "branch_id", branchId
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(locationName))
            .andExpect(jsonPath("$.branch_id").value(branchId.toString()))
            .andReturn()
            .getResponse()
            .getContentAsString();
        return extractId(responseBody);
    }

    private UUID createDepartment(
        UUID tenantId,
        UUID authenticatedUserId,
        String departmentName
    ) throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/departments")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, authenticatedUserId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-department-create")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", departmentName))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(departmentName))
            .andReturn()
            .getResponse()
            .getContentAsString();
        return extractId(responseBody);
    }

    private UUID extractId(String responseBody) throws Exception {
        JsonNode response = objectMapper.readTree(responseBody);
        return UUID.fromString(response.get("id").asText());
    }
}
