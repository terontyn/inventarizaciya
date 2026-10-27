package com.itam.softwareservice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class SchemaConstraintIntegrationTest extends IntegrationTestSupport {

    @Test
    void migrationsRequireRecordNameAndRejectDuplicateCurrentUserRelation() {
        UUID tenantId = UUID.randomUUID();

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO software_records (id, tenant_id) VALUES (?, ?)",
                UUID.randomUUID(),
                tenantId
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        UUID recordId = insertSoftwareRecord(tenantId, "Office");
        UUID userId = UUID.randomUUID();
        insertSoftwareUserAllocation(tenantId, recordId, userId);

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO software_user_allocations (id, tenant_id, software_record_id, user_id) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(),
                tenantId,
                recordId,
                userId
            )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void migrationsRejectDuplicateCurrentAssetRelation() {
        UUID tenantId = UUID.randomUUID();
        UUID recordId = insertSoftwareRecord(tenantId, "Visual Studio Code");
        UUID assetId = UUID.randomUUID();
        insertSoftwareAssetAllocation(tenantId, recordId, assetId);

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO software_asset_allocations (id, tenant_id, software_record_id, asset_id) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(),
                tenantId,
                recordId,
                assetId
            )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
