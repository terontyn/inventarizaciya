package com.itam.userservice.infrastructure;

import com.itam.userservice.domain.Role;
import com.itam.userservice.domain.RoleRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcRoleRepository implements RoleRepository {

    private static final RowMapper<Role> ROLE_ROW_MAPPER = (resultSet, rowNum) -> new Role(
        resultSet.getObject("id", UUID.class),
        resultSet.getObject("tenant_id", UUID.class),
        resultSet.getString("name")
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcRoleRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Role create(UUID tenantId, String name) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO roles (id, tenant_id, name) VALUES (:id, :tenantId, :name)",
            Map.of("id", id, "tenantId", scopedTenantId, "name", name)
        );
        return new Role(id, scopedTenantId, name);
    }

    @Override
    public List<Role> findAll(UUID tenantId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, name FROM roles WHERE tenant_id = :tenantId ORDER BY name, id",
            Map.of("tenantId", scopedTenantId),
            ROLE_ROW_MAPPER
        );
    }

    @Override
    public Optional<Role> findById(UUID tenantId, UUID id) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        List<Role> roles = jdbcTemplate.query(
            "SELECT id, tenant_id, name FROM roles WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id),
            ROLE_ROW_MAPPER
        );
        return roles.stream().findFirst();
    }

    @Override
    public Optional<Role> update(UUID tenantId, UUID id, String name) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        int updated = jdbcTemplate.update(
            "UPDATE roles SET name = :name WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id, "name", name)
        );
        return updated == 0 ? Optional.empty() : findById(scopedTenantId, id);
    }

    @Override
    public boolean exists(UUID tenantId, UUID id) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM roles WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id),
            Integer.class
        );
        return count != null && count > 0;
    }
}
