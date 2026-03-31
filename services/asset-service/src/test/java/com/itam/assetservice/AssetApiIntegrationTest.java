package com.itam.assetservice;

import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_ACTOR_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.CORRELATION_ID_HEADER;
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
import org.springframework.http.HttpStatus;

class AssetApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void assetCrudStaysInsideTenantScopeAndGeneratesAudit() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID assignedUserId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();

        expectUserLookup(tenantA, assignedUserId, HttpStatus.OK);
        expectLocationLookup(tenantA, locationId, HttpStatus.OK);

        String createBody = mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
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

        mockUserService.verify();

        JsonNode createJson = objectMapper.readTree(createBody);
        UUID assetId = UUID.fromString(createJson.get("id").asText());

        mockMvc.perform(patch("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
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

        mockMvc.perform(get("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(assetId.toString()))
            .andExpect(jsonPath("$[0].asset_tag").value("LAP-001-UPDATED"));

        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()))
            .andExpect(jsonPath("$.assigned_user_id").value(assignedUserId.toString()))
            .andExpect(jsonPath("$.location_id").value(locationId.toString()));

        mockMvc.perform(get("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB))
            .andExpect(status().isNotFound());

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
    void assetTagIsUniqueWithinTenantButReusableAcrossTenants() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001",
                    "serial_number", "SN-001",
                    "asset_type", "Laptop",
                    "status", "IN_STOCK"
                ))))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001",
                    "serial_number", "SN-002",
                    "asset_type", "Laptop",
                    "status", "IN_STOCK"
                ))))
            .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001",
                    "serial_number", "SN-003",
                    "asset_type", "Laptop",
                    "status", "IN_STOCK"
                ))))
            .andExpect(status().isCreated());
    }

    @Test
    void createAssetRejectsAssignedUserOutsideTenantScope() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assignedUserId = UUID.randomUUID();

        expectUserLookup(tenantId, assignedUserId, HttpStatus.NOT_FOUND);

        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
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
        UUID assignedUserId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();

        expectUserLookup(tenantId, assignedUserId, HttpStatus.OK);
        expectLocationLookup(tenantId, locationId, HttpStatus.NOT_FOUND);

        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
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

        mockMvc.perform(patch("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001",
                    "serial_number", "SN-001",
                    "asset_type", "Laptop",
                    "status", "IN_STOCK",
                    "assigned_user_id", UUID.randomUUID()
                ))))
            .andExpect(status().isBadRequest());

        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void crossTenantUpdateIsRejected() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantA,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_STOCK",
            null,
            null
        );

        mockMvc.perform(patch("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "asset_tag", "LAP-001-UPDATED",
                    "serial_number", "SN-001-UPDATED",
                    "asset_type", "Laptop Pro",
                    "status", "IN_USE"
                ))))
            .andExpect(status().isNotFound());

        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }
}
