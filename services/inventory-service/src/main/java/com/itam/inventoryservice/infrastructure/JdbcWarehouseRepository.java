package com.itam.inventoryservice.infrastructure;

import com.itam.inventoryservice.domain.Warehouse;
import com.itam.inventoryservice.domain.WarehouseRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcWarehouseRepository implements WarehouseRepository {

    private static final RowMapper<Warehouse> WAREHOUSE_ROW_MAPPER = (resultSet, rowNum) -> new Warehouse(
        resultSet.getObject("id", UUID.class),
        resultSet.getObject("tenant_id", UUID.class),
        resultSet.getObject("branch_id", UUID.class),
        resultSet.getObject("location_id", UUID.class)
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcWarehouseRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Warehouse> findAll(UUID tenantId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, branch_id, location_id FROM warehouses WHERE tenant_id = :tenantId ORDER BY id",
            Map.of("tenantId", scopedTenantId),
            WAREHOUSE_ROW_MAPPER
        );
    }

    @Override
    public Optional<Warehouse> findById(UUID tenantId, UUID id) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        List<Warehouse> warehouses = jdbcTemplate.query(
            "SELECT id, tenant_id, branch_id, location_id FROM warehouses WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id),
            WAREHOUSE_ROW_MAPPER
        );
        return warehouses.stream().findFirst();
    }
}
