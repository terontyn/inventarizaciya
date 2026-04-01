package com.itam.assetservice;

import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_ACTOR_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.CORRELATION_ID_HEADER;
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
import org.springframework.http.HttpStatus;

class AssetResponsibilityApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void assignSuccessUpdatesMasterStatePersistsFactAndGeneratesAudit() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_STOCK",
            null,
            locationId
        );

        expectUserLookup(tenantId, targetUserId, HttpStatus.OK);

        mockMvc.perform(post("/api/v1/assets/{id}/assign", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
                .header(CORRELATION_ID_HEADER, "corr-assign")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_user_id", targetUserId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()))
            .andExpect(jsonPath("$.assigned_user_id").value(targetUserId.toString()));

        mockUserService.verify();

        AssetRow assetRow = assetRow(tenantId, assetId);
        assertThat(assetRow.assignedUserId()).isEqualTo(targetUserId);
        assertThat(assetRow.locationId()).isEqualTo(locationId);
        assertThat(countRows("asset_responsibility_facts")).isEqualTo(1);
        ResponsibilityFactRow factRow = latestResponsibilityFactRow();
        assertThat(factRow.tenantId()).isEqualTo(tenantId);
        assertThat(factRow.assetId()).isEqualTo(assetId);
        assertThat(factRow.factType()).isEqualTo("ASSIGN");
        assertThat(factRow.previousAssignedUserId()).isNull();
        assertThat(factRow.newAssignedUserId()).isEqualTo(targetUserId);
        assertThat(factRow.previousLocationId()).isEqualTo(locationId);
        assertThat(factRow.newLocationId()).isEqualTo(locationId);

        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.actionType()).isEqualTo("ASSIGN");
        assertThat(auditRow.correlationId()).isEqualTo("corr-assign");
        assertThat(auditRow.changePayload()).contains("\"assigned_user_id\":\"" + targetUserId + "\"");
    }

    @Test
    void assignRejectsCrossTenantOrMissingTargetUser() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_STOCK",
            null,
            null
        );

        expectUserLookup(tenantId, targetUserId, HttpStatus.NOT_FOUND);

        mockMvc.perform(post("/api/v1/assets/{id}/assign", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_user_id", targetUserId))))
            .andExpect(status().isBadRequest());

        mockUserService.verify();
        assertThat(assetRow(tenantId, assetId).assignedUserId()).isNull();
        assertThat(countRows("asset_responsibility_facts")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void transferUserOnlySuccess() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_USE",
            currentUserId,
            locationId
        );

        expectUserLookup(tenantId, targetUserId, HttpStatus.OK);

        mockMvc.perform(post("/api/v1/assets/{id}/transfer", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
                .header(CORRELATION_ID_HEADER, "corr-transfer-user")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_user_id", targetUserId))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()))
            .andExpect(jsonPath("$.assigned_user_id").value(targetUserId.toString()))
            .andExpect(jsonPath("$.location_id").value(locationId.toString()));

        mockUserService.verify();

        ResponsibilityFactRow factRow = latestResponsibilityFactRow();
        assertThat(factRow.factType()).isEqualTo("TRANSFER");
        assertThat(factRow.previousAssignedUserId()).isEqualTo(currentUserId);
        assertThat(factRow.newAssignedUserId()).isEqualTo(targetUserId);
        assertThat(factRow.previousLocationId()).isEqualTo(locationId);
        assertThat(factRow.newLocationId()).isEqualTo(locationId);

        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.actionType()).isEqualTo("TRANSFER");
        assertThat(auditRow.correlationId()).isEqualTo("corr-transfer-user");
    }

    @Test
    void transferLocationOnlySuccessWithoutCurrentAssignedUser() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID currentLocationId = UUID.randomUUID();
        UUID targetLocationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_STOCK",
            null,
            currentLocationId
        );

        expectLocationLookup(tenantId, targetLocationId, HttpStatus.OK);

        String responseBody = mockMvc.perform(post("/api/v1/assets/{id}/transfer", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
                .header(CORRELATION_ID_HEADER, "corr-transfer-location")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_location_id", targetLocationId))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        mockUserService.verify();

        JsonNode responseJson = objectMapper.readTree(responseBody);
        assertThat(responseJson.get("id").asText()).isEqualTo(assetId.toString());
        assertThat(responseJson.get("assigned_user_id").isNull()).isTrue();
        assertThat(responseJson.get("location_id").asText()).isEqualTo(targetLocationId.toString());

        AssetRow assetRow = assetRow(tenantId, assetId);
        assertThat(assetRow.assignedUserId()).isNull();
        assertThat(assetRow.locationId()).isEqualTo(targetLocationId);
        ResponsibilityFactRow factRow = latestResponsibilityFactRow();
        assertThat(factRow.factType()).isEqualTo("TRANSFER");
        assertThat(factRow.previousAssignedUserId()).isNull();
        assertThat(factRow.newAssignedUserId()).isNull();
        assertThat(factRow.previousLocationId()).isEqualTo(currentLocationId);
        assertThat(factRow.newLocationId()).isEqualTo(targetLocationId);
    }

    @Test
    void transferCombinedSuccess() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID currentLocationId = UUID.randomUUID();
        UUID targetLocationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_USE",
            currentUserId,
            currentLocationId
        );

        expectUserLookup(tenantId, targetUserId, HttpStatus.OK);
        expectLocationLookup(tenantId, targetLocationId, HttpStatus.OK);

        mockMvc.perform(post("/api/v1/assets/{id}/transfer", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "target_user_id", targetUserId,
                    "target_location_id", targetLocationId
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()))
            .andExpect(jsonPath("$.assigned_user_id").value(targetUserId.toString()))
            .andExpect(jsonPath("$.location_id").value(targetLocationId.toString()));

        mockUserService.verify();

        ResponsibilityFactRow factRow = latestResponsibilityFactRow();
        assertThat(factRow.factType()).isEqualTo("TRANSFER");
        assertThat(factRow.previousAssignedUserId()).isEqualTo(currentUserId);
        assertThat(factRow.newAssignedUserId()).isEqualTo(targetUserId);
        assertThat(factRow.previousLocationId()).isEqualTo(currentLocationId);
        assertThat(factRow.newLocationId()).isEqualTo(targetLocationId);
    }

    @Test
    void transferRejectsNoEffectiveChange() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID currentLocationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_USE",
            currentUserId,
            currentLocationId
        );

        expectUserLookup(tenantId, currentUserId, HttpStatus.OK);
        expectLocationLookup(tenantId, currentLocationId, HttpStatus.OK);

        mockMvc.perform(post("/api/v1/assets/{id}/transfer", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "target_user_id", currentUserId,
                    "target_location_id", currentLocationId
                ))))
            .andExpect(status().isBadRequest());

        mockUserService.verify();
        assertThat(countRows("asset_responsibility_facts")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void transferRejectsCreatingFirstUserResponsibility() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_STOCK",
            null,
            null
        );

        expectUserLookup(tenantId, targetUserId, HttpStatus.OK);

        mockMvc.perform(post("/api/v1/assets/{id}/transfer", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_user_id", targetUserId))))
            .andExpect(status().isBadRequest());

        mockUserService.verify();
        assertThat(assetRow(tenantId, assetId).assignedUserId()).isNull();
        assertThat(countRows("asset_responsibility_facts")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void transferRejectsMissingOrCrossTenantTargetLocation() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID currentLocationId = UUID.randomUUID();
        UUID targetLocationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_USE",
            currentUserId,
            currentLocationId
        );

        expectLocationLookup(tenantId, targetLocationId, HttpStatus.NOT_FOUND);

        mockMvc.perform(post("/api/v1/assets/{id}/transfer", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_location_id", targetLocationId))))
            .andExpect(status().isBadRequest());

        mockUserService.verify();
        assertThat(assetRow(tenantId, assetId).locationId()).isEqualTo(currentLocationId);
        assertThat(countRows("asset_responsibility_facts")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void returnSuccessClearsAssignedUserWithoutChangingLocation() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_USE",
            currentUserId,
            locationId
        );

        mockMvc.perform(post("/api/v1/assets/{id}/return", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
                .header(CORRELATION_ID_HEADER, "corr-return"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()));

        AssetRow assetRow = assetRow(tenantId, assetId);
        assertThat(assetRow.assignedUserId()).isNull();
        assertThat(assetRow.locationId()).isEqualTo(locationId);
        ResponsibilityFactRow factRow = latestResponsibilityFactRow();
        assertThat(factRow.factType()).isEqualTo("RETURN");
        assertThat(factRow.previousAssignedUserId()).isEqualTo(currentUserId);
        assertThat(factRow.newAssignedUserId()).isNull();
        assertThat(factRow.previousLocationId()).isEqualTo(locationId);
        assertThat(factRow.newLocationId()).isEqualTo(locationId);

        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.actionType()).isEqualTo("RETURN");
        assertThat(auditRow.correlationId()).isEqualTo("corr-return");

        String assetBody = mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        JsonNode assetJson = objectMapper.readTree(assetBody);
        assertThat(assetJson.get("assigned_user_id").isNull()).isTrue();
        assertThat(assetJson.get("location_id").asText()).isEqualTo(locationId.toString());
    }

    @Test
    void returnRejectsWhenThereIsNoCurrentAssignedUser() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_STOCK",
            null,
            UUID.randomUUID()
        );

        mockMvc.perform(post("/api/v1/assets/{id}/return", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin"))
            .andExpect(status().isBadRequest());

        assertThat(countRows("asset_responsibility_facts")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void retireSuccessClearsAssignedUserKeepsLocationAndDoesNotDeleteAsset() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_USE",
            currentUserId,
            locationId
        );

        mockMvc.perform(post("/api/v1/assets/{id}/retire", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
                .header(CORRELATION_ID_HEADER, "corr-retire"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()));

        AssetRow assetRow = assetRow(tenantId, assetId);
        assertThat(assetRow.assignedUserId()).isNull();
        assertThat(assetRow.locationId()).isEqualTo(locationId);

        ResponsibilityFactRow factRow = latestResponsibilityFactRow();
        assertThat(factRow.factType()).isEqualTo("RETIRE");
        assertThat(factRow.previousAssignedUserId()).isEqualTo(currentUserId);
        assertThat(factRow.newAssignedUserId()).isNull();
        assertThat(factRow.previousLocationId()).isEqualTo(locationId);
        assertThat(factRow.newLocationId()).isEqualTo(locationId);

        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.actionType()).isEqualTo("RETIRE");
        assertThat(auditRow.correlationId()).isEqualTo("corr-retire");

        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()))
            .andExpect(jsonPath("$.location_id").value(locationId.toString()));
    }

    @Test
    void retireSuccessWithoutAssignedUserStillAppendsFactAndKeepsLocation() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_STOCK",
            null,
            locationId
        );

        mockMvc.perform(post("/api/v1/assets/{id}/retire", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
                .header(CORRELATION_ID_HEADER, "corr-retire-no-user"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assetId.toString()));

        AssetRow assetRow = assetRow(tenantId, assetId);
        assertThat(assetRow.assignedUserId()).isNull();
        assertThat(assetRow.locationId()).isEqualTo(locationId);

        ResponsibilityFactRow factRow = latestResponsibilityFactRow();
        assertThat(factRow.factType()).isEqualTo("RETIRE");
        assertThat(factRow.previousAssignedUserId()).isNull();
        assertThat(factRow.newAssignedUserId()).isNull();
        assertThat(factRow.previousLocationId()).isEqualTo(locationId);
        assertThat(factRow.newLocationId()).isEqualTo(locationId);

        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.actionType()).isEqualTo("RETIRE");
        assertThat(auditRow.correlationId()).isEqualTo("corr-retire-no-user");
    }

    @Test
    void commandEndpointsStayInsideTenantScope() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantA,
            "LAP-001",
            "SN-001",
            "Laptop",
            "IN_USE",
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        mockMvc.perform(post("/api/v1/assets/{id}/assign", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_user_id", UUID.randomUUID()))))
            .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/assets/{id}/transfer", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_location_id", UUID.randomUUID()))))
            .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/assets/{id}/return", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin"))
            .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/assets/{id}/retire", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantB)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-admin"))
            .andExpect(status().isNotFound());

        mockUserService.verify();
        assertThat(countRows("asset_responsibility_facts")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }
}
