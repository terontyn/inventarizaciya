package com.itam.assetservice;

import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.itam.assetservice.application.PilotRoleNames;
import com.itam.assetservice.domain.AssetResponsibilityFactType;
import com.itam.assetservice.domain.AssetStatus;
import com.itam.assetservice.domain.AuditAction;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class AssetHistoryAuditApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void allPilotReadRolesCanReadEmptyHistoryAndAudit() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null
        );
        UUID tenantAdminId = UUID.randomUUID();
        UUID assetOperatorId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        performHistoryRequest(tenantId, tenantAdminId, assetId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.items.length()").value(0));

        performHistoryRequest(tenantId, assetOperatorId, assetId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.items.length()").value(0));

        performHistoryRequest(tenantId, supportReviewerId, assetId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.items.length()").value(0));

        performAuditRequest(tenantId, tenantAdminId, assetId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.items.length()").value(0));

        performAuditRequest(tenantId, assetOperatorId, assetId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.items.length()").value(0));

        performAuditRequest(tenantId, supportReviewerId, assetId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.items.length()").value(0));

        mockUserService.verify();
    }

    @Test
    void historyEndpointReturnsStoredResponsibilityFactsNewestFirst() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            AssetStatus.RETIRED,
            null,
            UUID.fromString("00000000-0000-0000-0000-000000000004")
        );
        UUID supportReviewerId = UUID.randomUUID();

        insertResponsibilityFact(
            tenantId,
            assetId,
            AssetResponsibilityFactType.ASSIGN,
            null,
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            null,
            UUID.fromString("00000000-0000-0000-0000-000000000002"),
            Instant.parse("2026-01-01T10:00:00Z")
        );
        insertResponsibilityFact(
            tenantId,
            assetId,
            AssetResponsibilityFactType.TRANSFER,
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            UUID.fromString("00000000-0000-0000-0000-000000000003"),
            UUID.fromString("00000000-0000-0000-0000-000000000002"),
            UUID.fromString("00000000-0000-0000-0000-000000000004"),
            Instant.parse("2026-01-02T10:00:00Z")
        );
        insertResponsibilityFact(
            tenantId,
            assetId,
            AssetResponsibilityFactType.RETURN,
            UUID.fromString("00000000-0000-0000-0000-000000000003"),
            null,
            UUID.fromString("00000000-0000-0000-0000-000000000004"),
            UUID.fromString("00000000-0000-0000-0000-000000000004"),
            Instant.parse("2026-01-03T10:00:00Z")
        );
        insertResponsibilityFact(
            tenantId,
            assetId,
            AssetResponsibilityFactType.RETIRE,
            null,
            null,
            UUID.fromString("00000000-0000-0000-0000-000000000004"),
            UUID.fromString("00000000-0000-0000-0000-000000000004"),
            Instant.parse("2026-01-04T10:00:00Z")
        );

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        performHistoryRequest(tenantId, supportReviewerId, assetId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.items.length()").value(4))
            .andExpect(jsonPath("$.items[0].fact_type").value("RETIRE"))
            .andExpect(jsonPath("$.items[0].occurred_at").value("2026-01-04T10:00:00Z"))
            .andExpect(jsonPath("$.items[0].previous_assigned_user_id").value(Matchers.nullValue()))
            .andExpect(jsonPath("$.items[0].new_assigned_user_id").value(Matchers.nullValue()))
            .andExpect(jsonPath("$.items[0].previous_location_id").value("00000000-0000-0000-0000-000000000004"))
            .andExpect(jsonPath("$.items[0].new_location_id").value("00000000-0000-0000-0000-000000000004"))
            .andExpect(jsonPath("$.items[1].fact_type").value("RETURN"))
            .andExpect(jsonPath("$.items[1].occurred_at").value("2026-01-03T10:00:00Z"))
            .andExpect(jsonPath("$.items[1].previous_assigned_user_id").value("00000000-0000-0000-0000-000000000003"))
            .andExpect(jsonPath("$.items[1].new_assigned_user_id").value(Matchers.nullValue()))
            .andExpect(jsonPath("$.items[2].fact_type").value("TRANSFER"))
            .andExpect(jsonPath("$.items[2].new_assigned_user_id").value("00000000-0000-0000-0000-000000000003"))
            .andExpect(jsonPath("$.items[2].new_location_id").value("00000000-0000-0000-0000-000000000004"))
            .andExpect(jsonPath("$.items[3].fact_type").value("ASSIGN"))
            .andExpect(jsonPath("$.items[3].occurred_at").value("2026-01-01T10:00:00Z"))
            .andExpect(jsonPath("$.items[3].previous_assigned_user_id").value(Matchers.nullValue()))
            .andExpect(jsonPath("$.items[3].new_assigned_user_id").value("00000000-0000-0000-0000-000000000001"));

        mockUserService.verify();
    }

    @Test
    void auditEndpointReturnsStoredAssetAuditRecordsNewestFirst() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            AssetStatus.RETIRED,
            null,
            UUID.fromString("00000000-0000-0000-0000-000000000004")
        );
        UUID supportReviewerId = UUID.randomUUID();

        insertAssetAuditRecord(
            tenantId,
            assetId,
            AuditAction.CREATE,
            "tenant-admin",
            "corr-create",
            "{\"step\":\"create\"}",
            Instant.parse("2026-02-01T10:00:00Z")
        );
        insertAssetAuditRecord(
            tenantId,
            assetId,
            AuditAction.UPDATE,
            "tenant-admin",
            "corr-update",
            "{\"step\":\"update\"}",
            Instant.parse("2026-02-02T10:00:00Z")
        );
        insertAssetAuditRecord(
            tenantId,
            assetId,
            AuditAction.ASSIGN,
            "asset-operator",
            "corr-assign",
            "{\"step\":\"assign\"}",
            Instant.parse("2026-02-03T10:00:00Z")
        );
        insertAssetAuditRecord(
            tenantId,
            assetId,
            AuditAction.TRANSFER,
            "asset-operator",
            "corr-transfer",
            "{\"step\":\"transfer\"}",
            Instant.parse("2026-02-04T10:00:00Z")
        );
        insertAssetAuditRecord(
            tenantId,
            assetId,
            AuditAction.RETURN,
            "asset-operator",
            "corr-return",
            "{\"step\":\"return\"}",
            Instant.parse("2026-02-05T10:00:00Z")
        );
        insertAssetAuditRecord(
            tenantId,
            assetId,
            AuditAction.RETIRE,
            "system-retire",
            "corr-retire",
            "{\"step\":\"retire\"}",
            Instant.parse("2026-02-06T10:00:00Z")
        );

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        performAuditRequest(tenantId, supportReviewerId, assetId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.items.length()").value(6))
            .andExpect(jsonPath("$.items[0].action").value("RETIRE"))
            .andExpect(jsonPath("$.items[0].entity").value("ASSET"))
            .andExpect(jsonPath("$.items[0].entity_id").value(assetId.toString()))
            .andExpect(jsonPath("$.items[0].actor").value("system-retire"))
            .andExpect(jsonPath("$.items[0].correlation_id").value("corr-retire"))
            .andExpect(jsonPath("$.items[0].occurred_at").value("2026-02-06T10:00:00Z"))
            .andExpect(jsonPath("$.items[0].payload").value("{\"step\":\"retire\"}"))
            .andExpect(jsonPath("$.items[1].action").value("RETURN"))
            .andExpect(jsonPath("$.items[2].action").value("TRANSFER"))
            .andExpect(jsonPath("$.items[3].action").value("ASSIGN"))
            .andExpect(jsonPath("$.items[4].action").value("UPDATE"))
            .andExpect(jsonPath("$.items[5].action").value("CREATE"))
            .andExpect(jsonPath("$.items[5].correlation_id").value("corr-create"));

        mockUserService.verify();
    }

    @Test
    void historyAndAuditEndpointsAreTenantScoped() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantA,
            "LAP-001",
            "SN-001",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null
        );
        UUID supportReviewerId = UUID.randomUUID();

        expectRoleLookup(tenantB, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantB, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        performHistoryRequest(tenantB, supportReviewerId, assetId)
            .andExpect(status().isNotFound());

        performAuditRequest(tenantB, supportReviewerId, assetId)
            .andExpect(status().isNotFound());

        mockUserService.verify();
    }

    private org.springframework.test.web.servlet.ResultActions performHistoryRequest(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID assetId
    ) throws Exception {
        return mockMvc.perform(get("/api/v1/assets/{id}/history", assetId)
            .header(AUTHENTICATED_TENANT_HEADER, tenantId)
            .header(AUTHENTICATED_USER_HEADER, authenticatedUserId));
    }

    private org.springframework.test.web.servlet.ResultActions performAuditRequest(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID assetId
    ) throws Exception {
        return mockMvc.perform(get("/api/v1/assets/{id}/audit", assetId)
            .header(AUTHENTICATED_TENANT_HEADER, tenantId)
            .header(AUTHENTICATED_USER_HEADER, authenticatedUserId));
    }
}
