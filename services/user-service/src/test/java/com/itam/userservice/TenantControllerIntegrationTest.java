package com.itam.userservice;

import static com.itam.userservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_ACTOR_HEADER;
import static com.itam.userservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.userservice.api.AuthenticatedRequestContextExtractor.CORRELATION_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TenantControllerIntegrationTest extends IntegrationTestSupport {

    @Test
    void createTenantPersistsAndWritesAudit() throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/tenants")
                .header(AUTHENTICATED_ACTOR_HEADER, "bootstrap-admin")
                .header(CORRELATION_ID_HEADER, "corr-tenant-create")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", "Tenant One"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Tenant One"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode responseJson = objectMapper.readTree(responseBody);
        UUID tenantId = UUID.fromString(responseJson.get("id").asText());

        assertThat(countRows("tenants")).isEqualTo(1);
        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.tenantId()).isEqualTo(tenantId);
        assertThat(auditRow.actor()).isEqualTo("bootstrap-admin");
        assertThat(auditRow.entityType()).isEqualTo("TENANT");
        assertThat(auditRow.actionType()).isEqualTo("CREATE");
        assertThat(auditRow.correlationId()).isEqualTo("corr-tenant-create");
        assertThat(auditRow.changePayload()).contains("\"name\":\"Tenant One\"");
    }

    @Test
    void getTenantRejectsAuthenticatedTenantMismatch() throws Exception {
        UUID requestedTenantId = insertTenant("Requested");
        UUID authenticatedTenantId = insertTenant("Authenticated");

        mockMvc.perform(get("/api/v1/tenants/{id}", requestedTenantId)
                .header(AUTHENTICATED_TENANT_HEADER, authenticatedTenantId))
            .andExpect(status().isForbidden());
    }

    @Test
    void getTenantSucceedsWhenAuthenticatedTenantMatchesPathId() throws Exception {
        UUID tenantId = insertTenant("Tenant One");

        mockMvc.perform(get("/api/v1/tenants/{id}", tenantId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(tenantId.toString()))
            .andExpect(jsonPath("$.name").value("Tenant One"));
    }

    @Test
    void getTenantFailsFastWhenAuthenticatedTenantContextIsMissing() throws Exception {
        UUID tenantId = insertTenant("Tenant One");

        mockMvc.perform(get("/api/v1/tenants/{id}", tenantId))
            .andExpect(status().isBadRequest());
    }
}
