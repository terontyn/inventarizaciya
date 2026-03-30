package com.itam.userservice.infrastructure;

import com.itam.userservice.domain.Branch;
import com.itam.userservice.domain.BranchRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcBranchRepository implements BranchRepository {

    private static final RowMapper<Branch> BRANCH_ROW_MAPPER = (resultSet, rowNum) -> new Branch(
        resultSet.getObject("id", UUID.class),
        resultSet.getObject("tenant_id", UUID.class),
        resultSet.getString("name")
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcBranchRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Branch create(UUID tenantId, String name) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO branches (id, tenant_id, name) VALUES (:id, :tenantId, :name)",
            Map.of("id", id, "tenantId", scopedTenantId, "name", name)
        );
        return new Branch(id, scopedTenantId, name);
    }

    @Override
    public List<Branch> findAll(UUID tenantId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, name FROM branches WHERE tenant_id = :tenantId ORDER BY name, id",
            Map.of("tenantId", scopedTenantId),
            BRANCH_ROW_MAPPER
        );
    }

    @Override
    public Optional<Branch> findById(UUID tenantId, UUID id) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        List<Branch> branches = jdbcTemplate.query(
            "SELECT id, tenant_id, name FROM branches WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id),
            BRANCH_ROW_MAPPER
        );
        return branches.stream().findFirst();
    }

    @Override
    public Optional<Branch> update(UUID tenantId, UUID id, String name) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        int updated = jdbcTemplate.update(
            "UPDATE branches SET name = :name WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id, "name", name)
        );
        return updated == 0 ? Optional.empty() : findById(scopedTenantId, id);
    }

    @Override
    public boolean exists(UUID tenantId, UUID id) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM branches WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id),
            Integer.class
        );
        return count != null && count > 0;
    }
}
