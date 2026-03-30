package com.itam.userservice.infrastructure;

import com.itam.userservice.domain.Department;
import com.itam.userservice.domain.DepartmentRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcDepartmentRepository implements DepartmentRepository {

    private static final RowMapper<Department> DEPARTMENT_ROW_MAPPER = (resultSet, rowNum) -> new Department(
        resultSet.getObject("id", UUID.class),
        resultSet.getObject("tenant_id", UUID.class),
        resultSet.getString("name")
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcDepartmentRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Department create(UUID tenantId, String name) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO departments (id, tenant_id, name) VALUES (:id, :tenantId, :name)",
            Map.of("id", id, "tenantId", scopedTenantId, "name", name)
        );
        return new Department(id, scopedTenantId, name);
    }

    @Override
    public List<Department> findAll(UUID tenantId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, name FROM departments WHERE tenant_id = :tenantId ORDER BY name, id",
            Map.of("tenantId", scopedTenantId),
            DEPARTMENT_ROW_MAPPER
        );
    }

    @Override
    public Optional<Department> findById(UUID tenantId, UUID id) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        List<Department> departments = jdbcTemplate.query(
            "SELECT id, tenant_id, name FROM departments WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id),
            DEPARTMENT_ROW_MAPPER
        );
        return departments.stream().findFirst();
    }

    @Override
    public Optional<Department> update(UUID tenantId, UUID id, String name) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        int updated = jdbcTemplate.update(
            "UPDATE departments SET name = :name WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id, "name", name)
        );
        return updated == 0 ? Optional.empty() : findById(scopedTenantId, id);
    }
}
