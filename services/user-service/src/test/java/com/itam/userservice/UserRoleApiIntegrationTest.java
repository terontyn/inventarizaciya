package com.itam.userservice;

import static com.itam.userservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_ACTOR_HEADER;
import static com.itam.userservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.userservice.api.AuthenticatedRequestContextExtractor.CORRELATION_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserRoleApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void userEndpointsStayInsideTenantScopeAndGenerateAudit() throws Exception {
        UUID tenantA = insertTenant("Tenant A");
        UUID tenantB = insertTenant("Tenant B");
        UUID departmentOne = insertDepartment(tenantA, "Finance");
        UUID departmentTwo = insertDepartment(tenantA, "IT");

        String createBody = mockMvc.perform(post("/api/v1/users")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_ACTOR_HEADER, "user-admin")
                .header(CORRELATION_ID_HEADER, "corr-user-create")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Alice",
                    "department_id", departmentOne
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Alice"))
            .andExpect(jsonPath("$.department_id").value(departmentOne.toString()))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode createJson = objectMapper.readTree(createBody);
        UUID userId = UUID.fromString(createJson.get("id").asText());

        mockMvc.perform(patch("/api/v1/users/{id}", userId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_ACTOR_HEADER, "user-admin")
                .header(CORRELATION_ID_HEADER, "corr-user-update")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Alice Smith",
                    "department_id", departmentTwo
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.name").value("Alice Smith"))
            .andExpect(jsonPath("$.department_id").value(departmentTwo.toString()));

        mockMvc.perform(get("/api/v1/users")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(userId.toString()))
            .andExpect(jsonPath("$[0].department_id").value(departmentTwo.toString()));

        mockMvc.perform(get("/api/v1/users/{id}", userId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.name").value("Alice Smith"))
            .andExpect(jsonPath("$.department_id").value(departmentTwo.toString()));

        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/users")
                .header(AUTHENTICATED_TENANT_HEADER, tenantB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/v1/users/{id}", userId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB))
            .andExpect(status().isNotFound());

        assertThat(countRows("local_audit_records")).isEqualTo(2);
        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.entityType()).isEqualTo("USER");
        assertThat(auditRow.actionType()).isEqualTo("UPDATE");
        assertThat(auditRow.correlationId()).isEqualTo("corr-user-update");
        assertThat(auditRow.changePayload()).contains("\"department_id\":\"" + departmentTwo + "\"");
    }

    @Test
    void userRejectsCrossTenantDepartmentReference() throws Exception {
        UUID tenantA = insertTenant("Tenant A");
        UUID tenantB = insertTenant("Tenant B");
        UUID foreignDepartmentId = insertDepartment(tenantB, "Foreign Department");

        mockMvc.perform(post("/api/v1/users")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_ACTOR_HEADER, "user-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Alice",
                    "department_id", foreignDepartmentId
                ))))
            .andExpect(status().isBadRequest());
    }

    @Test
    void roleEndpointsStayInsideTenantScopeAndGenerateAudit() throws Exception {
        UUID tenantA = insertTenant("Tenant A");
        UUID tenantB = insertTenant("Tenant B");

        String createBody = mockMvc.perform(post("/api/v1/roles")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_ACTOR_HEADER, "role-admin")
                .header(CORRELATION_ID_HEADER, "corr-role-create")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Manager"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Manager"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        UUID roleId = UUID.fromString(objectMapper.readTree(createBody).get("id").asText());

        mockMvc.perform(patch("/api/v1/roles/{id}", roleId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_ACTOR_HEADER, "role-admin")
                .header(CORRELATION_ID_HEADER, "corr-role-update")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Manager Lead"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(roleId.toString()))
            .andExpect(jsonPath("$.name").value("Manager Lead"));

        mockMvc.perform(get("/api/v1/roles")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(roleId.toString()))
            .andExpect(jsonPath("$[0].name").value("Manager Lead"));

        mockMvc.perform(get("/api/v1/roles/{id}", roleId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(roleId.toString()))
            .andExpect(jsonPath("$.name").value("Manager Lead"));

        mockMvc.perform(get("/api/v1/roles")
                .header(AUTHENTICATED_TENANT_HEADER, tenantB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/v1/roles/{id}", roleId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB))
            .andExpect(status().isNotFound());

        assertThat(countRows("local_audit_records")).isEqualTo(2);
        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.entityType()).isEqualTo("ROLE");
        assertThat(auditRow.actionType()).isEqualTo("UPDATE");
        assertThat(auditRow.correlationId()).isEqualTo("corr-role-update");
    }
}
