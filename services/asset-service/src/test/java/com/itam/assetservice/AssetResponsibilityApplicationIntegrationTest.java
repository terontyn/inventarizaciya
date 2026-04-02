package com.itam.assetservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.itam.assetservice.application.AssetResponsibilityApplicationService;
import com.itam.assetservice.application.AuditMetadata;
import com.itam.assetservice.domain.AssetStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

class AssetResponsibilityApplicationIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private AssetResponsibilityApplicationService assetResponsibilityApplicationService;

    @Test
    void retireSetsRetiredStatusClearsActiveResponsibilityKeepsLocationAndDoesNotDeleteAsset() {
        UUID tenantId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        UUID assetId = insertAsset(
            tenantId,
            "LAP-001",
            "SN-001",
            "Laptop",
            AssetStatus.ACTIVE,
            currentUserId,
            locationId
        );

        AssetResponsibilityApplicationService.RetireResult result = assetResponsibilityApplicationService.retire(
            tenantId,
            assetId,
            new AuditMetadata("system-retire", "corr-retire")
        );

        assertThat(result.id()).isEqualTo(assetId);

        AssetRow assetRow = assetRow(tenantId, assetId);
        assertThat(assetRow.id()).isEqualTo(assetId);
        assertThat(assetRow.assignedUserId()).isNull();
        assertThat(assetRow.locationId()).isEqualTo(locationId);
        assertThat(assetRow.status()).isEqualTo(AssetStatus.RETIRED);

        ResponsibilityFactRow factRow = latestResponsibilityFactRow();
        assertThat(factRow.factType()).isEqualTo("RETIRE");
        assertThat(factRow.previousAssignedUserId()).isEqualTo(currentUserId);
        assertThat(factRow.newAssignedUserId()).isNull();
        assertThat(factRow.previousLocationId()).isEqualTo(locationId);
        assertThat(factRow.newLocationId()).isEqualTo(locationId);

        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.actionType()).isEqualTo("RETIRE");
        assertThat(auditRow.correlationId()).isEqualTo("corr-retire");
        assertThat(auditRow.changePayload()).contains("\"id\":\"" + assetId + "\"");
    }
}
