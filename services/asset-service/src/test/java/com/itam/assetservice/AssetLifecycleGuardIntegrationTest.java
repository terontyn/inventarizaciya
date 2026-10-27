package com.itam.assetservice;

import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_ACTOR_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.CORRELATION_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.itam.assetservice.application.PilotRoleNames;
import com.itam.assetservice.domain.AssetStatus;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * Covers lifecycle guard fixes: retired assets cannot be assigned or transferred
 * to a user, return does not resurrect retired assets, parent_asset_id is
 * validated within the tenant scope, and qr_data is an immutable id-based payload.
 */
class AssetLifecycleGuardIntegrationTest extends IntegrationTestSupport {

    @Test
    void assignRejectsRetiredAssetAndWritesNoFactOrAudit() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-RETIRED-ASSIGN",
            "SN-RETIRED-ASSIGN",
            "Laptop",
            AssetStatus.RETIRED,
            null,
            null
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        int factsBefore = countRows("asset_responsibility_facts");
        int auditBefore = countRows("local_audit_records");

        mockMvc.perform(post("/api/v1/assets/{id}/assign", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-retired-assign")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_user_id", targetUserId))))
            .andExpect(status().isConflict());

        AssetRow assetRow = assetRow(tenantId, assetId);
        assertThat(assetRow.status()).isEqualTo(AssetStatus.RETIRED);
        assertThat(assetRow.assignedUserId()).isNull();
        assertThat(countRows("asset_responsibility_facts")).isEqualTo(factsBefore);
        assertThat(countRows("local_audit_records")).isEqualTo(auditBefore);
    }

    @Test
    void transferToUserRejectsRetiredAsset() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-RETIRED-TRANSFER",
            "SN-RETIRED-TRANSFER",
            "Laptop",
            AssetStatus.RETIRED,
            null,
            null
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        mockMvc.perform(post("/api/v1/assets/{id}/transfer", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-retired-transfer")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_user_id", targetUserId))))
            .andExpect(status().isConflict());

        assertThat(assetRow(tenantId, assetId).assignedUserId()).isNull();
        assertThat(assetRow(tenantId, assetId).status()).isEqualTo(AssetStatus.RETIRED);
    }

    @Test
    void transferLocationOnlyStaysAllowedForRetiredAsset() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID targetLocationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-RETIRED-MOVE",
            "SN-RETIRED-MOVE",
            "Laptop",
            AssetStatus.RETIRED,
            null,
            UUID.randomUUID()
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectLocationLookup(tenantId, tenantAdminId, targetLocationId, HttpStatus.OK);

        mockMvc.perform(post("/api/v1/assets/{id}/transfer", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-retired-move")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("target_location_id", targetLocationId))))
            .andExpect(status().isOk());

        AssetRow assetRow = assetRow(tenantId, assetId);
        assertThat(assetRow.status()).isEqualTo(AssetStatus.RETIRED);
        assertThat(assetRow.locationId()).isEqualTo(targetLocationId);
    }

    @Test
    void returnDoesNotResurrectRetiredAsset() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assignedUserId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-RETIRED-RETURN",
            "SN-RETIRED-RETURN",
            "Laptop",
            AssetStatus.RETIRED,
            assignedUserId,
            null
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        mockMvc.perform(post("/api/v1/assets/{id}/return", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-retired-return")
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk());

        AssetRow assetRow = assetRow(tenantId, assetId);
        assertThat(assetRow.status()).isEqualTo(AssetStatus.RETIRED);
        assertThat(assetRow.assignedUserId()).isNull();
    }

    @Test
    void returnKeepsActiveAssetActive() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assignedUserId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-ACTIVE-RETURN",
            "SN-ACTIVE-RETURN",
            "Laptop",
            AssetStatus.ACTIVE,
            assignedUserId,
            null
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        mockMvc.perform(post("/api/v1/assets/{id}/return", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-active-return")
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk());

        AssetRow assetRow = assetRow(tenantId, assetId);
        assertThat(assetRow.status()).isEqualTo(AssetStatus.ACTIVE);
        assertThat(assetRow.assignedUserId()).isNull();
    }

    @Test
    void createRejectsUnknownParentAsset() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        Map<String, Object> payload = assetPayload("LAP-PARENT-MISSING", "SN-PARENT-MISSING", "Laptop");
        payload.put("parent_asset_id", UUID.randomUUID());

        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-parent-missing")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsParentAssetFromAnotherTenant() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID otherTenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID foreignParentId = insertAsset(
            otherTenantId,
            "LAP-FOREIGN-PARENT",
            "SN-FOREIGN-PARENT",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        Map<String, Object> payload = assetPayload("LAP-CROSS-PARENT", "SN-CROSS-PARENT", "Laptop");
        payload.put("parent_asset_id", foreignParentId);

        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-parent-cross")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createAcceptsParentAssetInSameTenant() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID parentAssetId = insertAsset(
            tenantId,
            "LAP-PARENT-OK",
            "SN-PARENT-OK",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        Map<String, Object> payload = assetPayload("DOCK-CHILD", "SN-DOCK-CHILD", "Dock");
        payload.put("parent_asset_id", parentAssetId);

        mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-parent-ok")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isCreated());
    }

    @Test
    void createStoresImmutableIdBasedQrPayload() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));

        Map<String, Object> payload = assetPayload("LAP-QR", "SN-QR", "Laptop");

        String body = mockMvc.perform(post("/api/v1/assets")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .header(CORRELATION_ID_HEADER, "corr-qr-create")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        UUID assetId = UUID.fromString(objectMapper.readTree(body).get("id").asText());
        String qrData = jdbcTemplate.queryForObject(
            "SELECT qr_data FROM assets WHERE tenant_id = ? AND id = ?",
            String.class,
            tenantId,
            assetId
        );
        assertThat(qrData).isEqualTo("ITAM-ASSET:" + assetId);
    }

    private Map<String, Object> assetPayload(String assetTag, String serialNumber, String assetType) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("asset_tag", assetTag);
        payload.put("serial_number", serialNumber);
        payload.put("asset_type", assetType);
        return payload;
    }
}
