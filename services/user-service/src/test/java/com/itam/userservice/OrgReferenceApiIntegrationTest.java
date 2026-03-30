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

class OrgReferenceApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void departmentEndpointsStayInsideTenantScope() throws Exception {
        UUID tenantA = insertTenant("Tenant A");
        UUID tenantB = insertTenant("Tenant B");
        UUID departmentId = insertDepartment(tenantA, "Finance");

        mockMvc.perform(get("/api/v1/departments")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(departmentId.toString()))
            .andExpect(jsonPath("$[0].name").value("Finance"));

        mockMvc.perform(get("/api/v1/departments")
                .header(AUTHENTICATED_TENANT_HEADER, tenantB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/v1/departments/{id}", departmentId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/departments"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void departmentCreateAndUpdateGenerateAudit() throws Exception {
        UUID tenantId = insertTenant("Tenant A");

        String createBody = mockMvc.perform(post("/api/v1/departments")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "org-admin")
                .header(CORRELATION_ID_HEADER, "corr-department-create")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Finance"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Finance"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        UUID departmentId = UUID.fromString(objectMapper.readTree(createBody).get("id").asText());

        mockMvc.perform(patch("/api/v1/departments/{id}", departmentId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "org-admin")
                .header(CORRELATION_ID_HEADER, "corr-department-update")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Finance North"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(departmentId.toString()))
            .andExpect(jsonPath("$.name").value("Finance North"));

        assertThat(countRows("local_audit_records")).isEqualTo(2);
        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.tenantId()).isEqualTo(tenantId);
        assertThat(auditRow.entityType()).isEqualTo("DEPARTMENT");
        assertThat(auditRow.actionType()).isEqualTo("UPDATE");
        assertThat(auditRow.correlationId()).isEqualTo("corr-department-update");
    }

    @Test
    void branchCreateAndUpdateGenerateAudit() throws Exception {
        UUID tenantId = insertTenant("Tenant A");

        String createBody = mockMvc.perform(post("/api/v1/branches")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "org-admin")
                .header(CORRELATION_ID_HEADER, "corr-branch-create")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "HQ"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("HQ"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        UUID branchId = UUID.fromString(objectMapper.readTree(createBody).get("id").asText());

        mockMvc.perform(patch("/api/v1/branches/{id}", branchId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "org-admin")
                .header(CORRELATION_ID_HEADER, "corr-branch-update")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "HQ North"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(branchId.toString()))
            .andExpect(jsonPath("$.name").value("HQ North"));

        mockMvc.perform(get("/api/v1/branches/{id}", branchId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(branchId.toString()))
            .andExpect(jsonPath("$.name").value("HQ North"));

        assertThat(countRows("local_audit_records")).isEqualTo(2);
        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.entityType()).isEqualTo("BRANCH");
        assertThat(auditRow.actionType()).isEqualTo("UPDATE");
    }

    @Test
    void locationLifecycleSupportsSameTenantBranchReferenceAndAudit() throws Exception {
        UUID tenantId = insertTenant("Tenant A");
        UUID branchOne = insertBranch(tenantId, "HQ");
        UUID branchTwo = insertBranch(tenantId, "Remote");

        String createBody = mockMvc.perform(post("/api/v1/locations")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "org-admin")
                .header(CORRELATION_ID_HEADER, "corr-location-create")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Room 101",
                    "branch_id", branchOne
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Room 101"))
            .andExpect(jsonPath("$.branch_id").value(branchOne.toString()))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode createJson = objectMapper.readTree(createBody);
        UUID locationId = UUID.fromString(createJson.get("id").asText());

        mockMvc.perform(patch("/api/v1/locations/{id}", locationId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "org-admin")
                .header(CORRELATION_ID_HEADER, "corr-location-update")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Room 102",
                    "branch_id", branchTwo
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(locationId.toString()))
            .andExpect(jsonPath("$.name").value("Room 102"))
            .andExpect(jsonPath("$.branch_id").value(branchTwo.toString()));

        mockMvc.perform(get("/api/v1/locations")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(locationId.toString()))
            .andExpect(jsonPath("$[0].branch_id").value(branchTwo.toString()));

        mockMvc.perform(get("/api/v1/locations/{id}", locationId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(locationId.toString()))
            .andExpect(jsonPath("$.branch_id").value(branchTwo.toString()));

        assertThat(countRows("local_audit_records")).isEqualTo(2);
        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.entityType()).isEqualTo("LOCATION");
        assertThat(auditRow.actionType()).isEqualTo("UPDATE");
        assertThat(auditRow.correlationId()).isEqualTo("corr-location-update");
    }

    @Test
    void locationRejectsCrossTenantBranchReference() throws Exception {
        UUID tenantA = insertTenant("Tenant A");
        UUID tenantB = insertTenant("Tenant B");
        UUID foreignBranchId = insertBranch(tenantB, "Foreign Branch");

        mockMvc.perform(post("/api/v1/locations")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_ACTOR_HEADER, "org-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Room 101",
                    "branch_id", foreignBranchId
                ))))
            .andExpect(status().isBadRequest());
    }

    @Test
    void locationRejectsInvalidBranchReference() throws Exception {
        UUID tenantId = insertTenant("Tenant A");

        mockMvc.perform(post("/api/v1/locations")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "org-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Room 101",
                    "branch_id", UUID.randomUUID()
                ))))
            .andExpect(status().isBadRequest());
    }
}
