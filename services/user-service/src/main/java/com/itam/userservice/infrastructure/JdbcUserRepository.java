package com.itam.userservice.infrastructure;

import com.itam.userservice.domain.User;
import com.itam.userservice.domain.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUserRepository implements UserRepository {

    private static final RowMapper<User> USER_ROW_MAPPER = (resultSet, rowNum) -> new User(
        resultSet.getObject("id", UUID.class),
        resultSet.getObject("tenant_id", UUID.class),
        resultSet.getString("name"),
        resultSet.getObject("department_id", UUID.class)
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcUserRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(UUID tenantId, String name, UUID departmentId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        UUID id = UUID.randomUUID();
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("tenantId", scopedTenantId)
            .addValue("name", name)
            .addValue("departmentId", departmentId);
        jdbcTemplate.update(
            "INSERT INTO users (id, tenant_id, name, department_id) "
                + "VALUES (:id, :tenantId, :name, :departmentId)",
            parameters
        );
        return new User(id, scopedTenantId, name, departmentId);
    }

    @Override
    public List<User> findAll(UUID tenantId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, name, department_id "
                + "FROM users WHERE tenant_id = :tenantId ORDER BY name, id",
            Map.of("tenantId", scopedTenantId),
            USER_ROW_MAPPER
        );
    }

    @Override
    public Optional<User> findById(UUID tenantId, UUID id) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        List<User> users = jdbcTemplate.query(
            "SELECT id, tenant_id, name, department_id "
                + "FROM users WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id),
            USER_ROW_MAPPER
        );
        return users.stream().findFirst();
    }

    @Override
    public Optional<User> update(UUID tenantId, UUID id, String name, UUID departmentId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("tenantId", scopedTenantId)
            .addValue("id", id)
            .addValue("name", name)
            .addValue("departmentId", departmentId);
        int updated = jdbcTemplate.update(
            "UPDATE users SET name = :name, department_id = :departmentId "
                + "WHERE tenant_id = :tenantId AND id = :id",
            parameters
        );
        return updated == 0 ? Optional.empty() : findById(scopedTenantId, id);
    }

    @Override
    public boolean exists(UUID tenantId, UUID id) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id),
            Integer.class
        );
        return count != null && count > 0;
    }
}
