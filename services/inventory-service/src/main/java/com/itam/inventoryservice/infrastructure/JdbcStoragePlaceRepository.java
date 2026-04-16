package com.itam.inventoryservice.infrastructure;

import com.itam.inventoryservice.domain.StoragePlace;
import com.itam.inventoryservice.domain.StoragePlaceRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcStoragePlaceRepository implements StoragePlaceRepository {

    private static final RowMapper<StoragePlace> STORAGE_PLACE_ROW_MAPPER = (resultSet, rowNum) -> new StoragePlace(
        resultSet.getObject("id", UUID.class),
        resultSet.getObject("tenant_id", UUID.class),
        resultSet.getObject("warehouse_id", UUID.class)
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcStoragePlaceRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<StoragePlace> findByWarehouseId(UUID tenantId, UUID warehouseId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, warehouse_id FROM storage_places "
                + "WHERE tenant_id = :tenantId AND warehouse_id = :warehouseId ORDER BY id",
            Map.of("tenantId", scopedTenantId, "warehouseId", warehouseId),
            STORAGE_PLACE_ROW_MAPPER
        );
    }

    @Override
    public Optional<StoragePlace> findById(UUID tenantId, UUID id) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        List<StoragePlace> storagePlaces = jdbcTemplate.query(
            "SELECT id, tenant_id, warehouse_id FROM storage_places WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id),
            STORAGE_PLACE_ROW_MAPPER
        );
        return storagePlaces.stream().findFirst();
    }
}
