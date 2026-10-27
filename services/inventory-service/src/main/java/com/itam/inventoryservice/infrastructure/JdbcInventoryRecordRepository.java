package com.itam.inventoryservice.infrastructure;

import com.itam.inventoryservice.domain.InventoryRecord;
import com.itam.inventoryservice.domain.InventoryRecordRepository;
import com.itam.inventoryservice.domain.ReadinessState;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcInventoryRecordRepository implements InventoryRecordRepository {

    private static final RowMapper<InventoryRecord> INVENTORY_RECORD_ROW_MAPPER = (resultSet, rowNum) -> new InventoryRecord(
        resultSet.getObject("id", UUID.class),
        resultSet.getObject("tenant_id", UUID.class),
        resultSet.getObject("asset_id", UUID.class),
        resultSet.getObject("warehouse_id", UUID.class),
        resultSet.getObject("storage_place_id", UUID.class),
        ReadinessState.valueOf(resultSet.getString("readiness_state"))
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcInventoryRecordRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<InventoryRecord> findAllCurrent(UUID tenantId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, asset_id, warehouse_id, storage_place_id, readiness_state "
                + "FROM inventory_records WHERE tenant_id = :tenantId ORDER BY asset_id, id",
            Map.of("tenantId", scopedTenantId),
            INVENTORY_RECORD_ROW_MAPPER
        );
    }

    @Override
    public Optional<InventoryRecord> findByAssetId(UUID tenantId, UUID assetId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        List<InventoryRecord> records = jdbcTemplate.query(
            "SELECT id, tenant_id, asset_id, warehouse_id, storage_place_id, readiness_state "
                + "FROM inventory_records WHERE tenant_id = :tenantId AND asset_id = :assetId",
            Map.of("tenantId", scopedTenantId, "assetId", assetId),
            INVENTORY_RECORD_ROW_MAPPER
        );
        return records.stream().findFirst();
    }

    @Override
    public InventoryRecord create(
        UUID tenantId,
        UUID assetId,
        UUID warehouseId,
        UUID storagePlaceId,
        ReadinessState readinessState
    ) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        UUID id = UUID.randomUUID();
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("tenantId", scopedTenantId)
            .addValue("assetId", assetId)
            .addValue("warehouseId", warehouseId)
            .addValue("storagePlaceId", storagePlaceId)
            .addValue("readinessState", readinessState.name());
        jdbcTemplate.update(
            "INSERT INTO inventory_records (id, tenant_id, asset_id, warehouse_id, storage_place_id, readiness_state) "
                + "VALUES (:id, :tenantId, :assetId, :warehouseId, :storagePlaceId, :readinessState)",
            parameters
        );
        return new InventoryRecord(id, scopedTenantId, assetId, warehouseId, storagePlaceId, readinessState);
    }

    @Override
    public Optional<InventoryRecord> updatePlacement(
        UUID tenantId,
        UUID assetId,
        UUID warehouseId,
        UUID storagePlaceId
    ) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        int updated = jdbcTemplate.update(
            "UPDATE inventory_records SET warehouse_id = :warehouseId, storage_place_id = :storagePlaceId "
                + "WHERE tenant_id = :tenantId AND asset_id = :assetId",
            Map.of(
                "tenantId", scopedTenantId,
                "assetId", assetId,
                "warehouseId", warehouseId,
                "storagePlaceId", storagePlaceId
            )
        );
        return updated == 0 ? Optional.empty() : findByAssetId(scopedTenantId, assetId);
    }

    @Override
    public Optional<InventoryRecord> updateReadiness(UUID tenantId, UUID assetId, ReadinessState readinessState) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        int updated = jdbcTemplate.update(
            "UPDATE inventory_records SET readiness_state = :readinessState "
                + "WHERE tenant_id = :tenantId AND asset_id = :assetId",
            Map.of(
                "tenantId", scopedTenantId,
                "assetId", assetId,
                "readinessState", readinessState.name()
            )
        );
        return updated == 0 ? Optional.empty() : findByAssetId(scopedTenantId, assetId);
    }

    @Override
    public void deleteById(UUID tenantId, UUID id) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        jdbcTemplate.update(
            "DELETE FROM inventory_records WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id)
        );
    }
}
