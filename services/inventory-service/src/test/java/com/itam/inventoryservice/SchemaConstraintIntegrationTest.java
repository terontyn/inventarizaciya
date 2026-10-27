package com.itam.inventoryservice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.itam.inventoryservice.domain.ReadinessState;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class SchemaConstraintIntegrationTest extends IntegrationTestSupport {

    @Test
    void migrationsEnforceWarehouseBranchRequirementAndOneCurrentInventoryRecordPerAsset() {
        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO warehouses (id) VALUES (?)",
                UUID.randomUUID()
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        UUID tenantId = UUID.randomUUID();
        UUID warehouseId = insertWarehouse(tenantId, UUID.randomUUID());
        UUID assetId = UUID.randomUUID();
        insertInventoryRecord(tenantId, assetId, warehouseId, null);

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO inventory_records (id, tenant_id, asset_id, warehouse_id) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(),
                tenantId,
                assetId,
                warehouseId
            )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void migrationsRejectCrossTenantWarehouseAndStoragePlaceReferences() {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID warehouseA = insertWarehouse(tenantA, UUID.randomUUID());
        UUID warehouseB = insertWarehouse(tenantB, UUID.randomUUID());
        UUID storagePlaceB = insertStoragePlace(tenantB, warehouseB);

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO storage_places (id, tenant_id, warehouse_id) VALUES (?, ?, ?)",
                UUID.randomUUID(),
                tenantA,
                warehouseB
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO inventory_records (id, tenant_id, asset_id, warehouse_id, storage_place_id) "
                    + "VALUES (?, ?, ?, ?, ?)",
                UUID.randomUUID(),
                tenantA,
                UUID.randomUUID(),
                warehouseA,
                storagePlaceB
            )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void migrationsRejectUnsupportedReadinessVocabulary() {
        UUID tenantId = UUID.randomUUID();
        UUID warehouseId = insertWarehouse(tenantId, UUID.randomUUID());

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO inventory_records (id, tenant_id, asset_id, warehouse_id, readiness_state) "
                    + "VALUES (?, ?, ?, ?, ?)",
                UUID.randomUUID(),
                tenantId,
                UUID.randomUUID(),
                warehouseId,
                "BROKEN"
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        insertInventoryRecord(tenantId, UUID.randomUUID(), warehouseId, null, ReadinessState.NOT_READY);
    }
}
