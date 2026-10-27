package com.itam.maintenanceservice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.itam.maintenanceservice.domain.MaintenanceWorkState;
import com.itam.maintenanceservice.domain.MaintenanceWorkType;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class SchemaConstraintIntegrationTest extends IntegrationTestSupport {

    @Test
    void migrationsRejectUnsupportedVocabularyAndCompletionConsistency() {
        UUID tenantId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO maintenance_works (id, tenant_id, asset_id, work_type, work_state, opened_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(),
                tenantId,
                assetId,
                "BROKEN",
                "OPEN",
                Timestamp.from(Instant.now())
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO maintenance_works (id, tenant_id, asset_id, work_type, work_state, opened_at, completed_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(),
                tenantId,
                assetId,
                MaintenanceWorkType.REPAIR.name(),
                MaintenanceWorkState.OPEN.name(),
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now())
            )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void migrationsAllowCompletedWorkRowsForSameAsset() {
        UUID tenantId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        insertMaintenanceWork(
            tenantId,
            assetId,
            MaintenanceWorkType.INSPECTION,
            MaintenanceWorkState.COMPLETED,
            Instant.parse("2026-04-16T08:00:00Z"),
            Instant.parse("2026-04-16T09:00:00Z")
        );
        insertMaintenanceWork(
            tenantId,
            assetId,
            MaintenanceWorkType.REPAIR,
            MaintenanceWorkState.COMPLETED,
            Instant.parse("2026-04-17T08:00:00Z"),
            Instant.parse("2026-04-17T10:00:00Z")
        );
    }
}
