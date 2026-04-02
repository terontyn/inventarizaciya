package com.itam.assetservice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.itam.assetservice.domain.AssetStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class SchemaConstraintIntegrationTest extends IntegrationTestSupport {

    @Test
    void migrationsEnforceTenantScopeAndUniqueAssetTag() {
        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO assets (id, asset_tag, serial_number, asset_type, status) VALUES (?, ?, ?, ?, ?)",
                UUID.randomUUID(),
                "LAP-001",
                "SN-001",
                "Laptop",
                AssetStatus.ACTIVE
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        UUID tenantId = UUID.randomUUID();
        insertAsset(tenantId, "LAP-001", "SN-001", "Laptop", AssetStatus.ACTIVE, null, null);

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO assets ("
                    + "id, tenant_id, asset_tag, serial_number, asset_type, status, assigned_user_id, location_id"
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(),
                tenantId,
                "LAP-001",
                "SN-002",
                "Laptop",
                AssetStatus.ACTIVE,
                null,
                null
            )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void migrationsRejectUnsupportedStatusVocabulary() {
        UUID tenantId = UUID.randomUUID();

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO assets (id, tenant_id, asset_tag, serial_number, asset_type, status) VALUES (?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(),
                tenantId,
                "LAP-001",
                "SN-001",
                "Laptop",
                "BROKEN"
            )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void responsibilityFactsRequireTenantScopedAssetReference() {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID assetId = insertAsset(tenantA, "LAP-001", "SN-001", "Laptop", AssetStatus.ACTIVE, null, null);

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO asset_responsibility_facts (id, asset_id, fact_type, occurred_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
                UUID.randomUUID(),
                assetId,
                "ASSIGN"
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO asset_responsibility_facts (id, tenant_id, asset_id, fact_type, occurred_at) "
                    + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",
                UUID.randomUUID(),
                tenantB,
                assetId,
                "ASSIGN"
            )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
