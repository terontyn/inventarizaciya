package com.itam.assetservice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import com.itam.assetservice.domain.AssetStatus;
import java.util.List;
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
    void identityFieldsAreNullableAndDisplayNameLengthIsBounded() {
        UUID tenantId = UUID.randomUUID();
        UUID nullableAssetId = insertAsset(
            tenantId,
            "LAP-NULL",
            "SN-NULL",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null
        );
        assertThat(assetRow(tenantId, nullableAssetId).displayName()).isNull();
        assertThat(assetRow(tenantId, nullableAssetId).note()).isNull();

        UUID maxDisplayNameAssetId = insertAsset(
            tenantId,
            "LAP-MAX",
            "SN-MAX",
            "Laptop",
            AssetStatus.ACTIVE,
            null,
            null,
            "d".repeat(160),
            "Plain operational note"
        );
        assertThat(assetRow(tenantId, maxDisplayNameAssetId).displayName()).hasSize(160);
        assertThat(assetRow(tenantId, maxDisplayNameAssetId).note()).isEqualTo("Plain operational note");

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO assets ("
                    + "id, tenant_id, asset_tag, serial_number, asset_type, status, display_name"
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(),
                tenantId,
                "LAP-TOO-LONG",
                "SN-TOO-LONG",
                "Laptop",
                AssetStatus.ACTIVE,
                "d".repeat(161)
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

    @Test
    void technicalAttributesTableEnforcesTenantAssetKeyAndValueConstraints() {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID assetId = insertAsset(tenantA, "LAP-TECH", "SN-TECH", "Laptop", AssetStatus.ACTIVE, null, null);

        assertThat(countRows("asset_technical_attributes")).isZero();

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO asset_technical_attributes ("
                    + "asset_id, attribute_key, attribute_value, created_at, updated_at"
                    + ") VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                assetId,
                "hostname",
                "host-01"
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO asset_technical_attributes ("
                    + "tenant_id, attribute_key, attribute_value, created_at, updated_at"
                    + ") VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                tenantA,
                "hostname",
                "host-01"
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO asset_technical_attributes ("
                    + "tenant_id, asset_id, attribute_value, created_at, updated_at"
                    + ") VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                tenantA,
                assetId,
                "host-01"
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO asset_technical_attributes ("
                    + "tenant_id, asset_id, attribute_key, created_at, updated_at"
                    + ") VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                tenantA,
                assetId,
                "hostname"
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        insertTechnicalAttribute(tenantA, assetId, "hostname", "host-01");
        assertThat(technicalAttributeValue(tenantA, assetId, "hostname")).isEqualTo("host-01");

        assertThatThrownBy(() ->
            insertTechnicalAttribute(tenantA, assetId, "hostname", "host-02")
        ).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() ->
            insertTechnicalAttribute(tenantA, UUID.randomUUID(), "ip_address", "10.0.0.25")
        ).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() ->
            insertTechnicalAttribute(tenantB, assetId, "ip_address", "10.0.0.25")
        ).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() ->
            insertTechnicalAttribute(tenantA, assetId, "anydesk_id", "a".repeat(513))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void technicalAttributesSearchScopeIndexExistsWithoutChangingCoreConstraints() {
        Integer indexCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.INDEXES "
                + "WHERE LOWER(TABLE_NAME) = 'asset_technical_attributes' "
                + "AND LOWER(INDEX_NAME) = 'idx_asset_technical_attributes_search_scope'",
            Integer.class
        );
        assertThat(indexCount).isEqualTo(1);

        List<String> indexColumns = jdbcTemplate.queryForList(
            "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.INDEX_COLUMNS "
                + "WHERE LOWER(TABLE_NAME) = 'asset_technical_attributes' "
                + "AND LOWER(INDEX_NAME) = 'idx_asset_technical_attributes_search_scope' "
                + "ORDER BY ORDINAL_POSITION",
            String.class
        );
        assertThat(indexColumns).containsExactly("tenant_id", "attribute_key");

        Integer primaryKeyCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS "
                + "WHERE LOWER(TABLE_NAME) = 'asset_technical_attributes' "
                + "AND CONSTRAINT_TYPE = 'PRIMARY KEY'",
            Integer.class
        );
        assertThat(primaryKeyCount).isEqualTo(1);
    }
}
