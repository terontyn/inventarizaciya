package com.itam.assetservice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                "IN_STOCK"
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        UUID tenantId = UUID.randomUUID();
        insertAsset(tenantId, "LAP-001", "SN-001", "Laptop", "IN_STOCK", null, null);

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
                "IN_STOCK",
                null,
                null
            )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
