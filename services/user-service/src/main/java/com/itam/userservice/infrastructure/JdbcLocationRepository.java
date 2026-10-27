package com.itam.userservice.infrastructure;

import com.itam.userservice.domain.Location;
import com.itam.userservice.domain.LocationRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcLocationRepository implements LocationRepository {

    private static final RowMapper<Location> LOCATION_ROW_MAPPER = (resultSet, rowNum) -> new Location(
        resultSet.getObject("id", UUID.class),
        resultSet.getObject("tenant_id", UUID.class),
        resultSet.getObject("branch_id", UUID.class),
        resultSet.getString("name")
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcLocationRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Location create(UUID tenantId, String name, UUID branchId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO locations (id, tenant_id, branch_id, name) "
                + "VALUES (:id, :tenantId, :branchId, :name)",
            Map.of("id", id, "tenantId", scopedTenantId, "branchId", branchId, "name", name)
        );
        return new Location(id, scopedTenantId, branchId, name);
    }

    @Override
    public List<Location> findAll(UUID tenantId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, branch_id, name FROM locations "
                + "WHERE tenant_id = :tenantId ORDER BY name, id",
            Map.of("tenantId", scopedTenantId),
            LOCATION_ROW_MAPPER
        );
    }

    @Override
    public Optional<Location> findById(UUID tenantId, UUID id) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        List<Location> locations = jdbcTemplate.query(
            "SELECT id, tenant_id, branch_id, name FROM locations "
                + "WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id),
            LOCATION_ROW_MAPPER
        );
        return locations.stream().findFirst();
    }

    @Override
    public Optional<Location> update(UUID tenantId, UUID id, String name, UUID branchId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        int updated = jdbcTemplate.update(
            "UPDATE locations SET name = :name, branch_id = :branchId "
                + "WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id, "name", name, "branchId", branchId)
        );
        return updated == 0 ? Optional.empty() : findById(scopedTenantId, id);
    }
}
