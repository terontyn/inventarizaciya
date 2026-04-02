package com.itam.userservice.infrastructure;

import com.itam.userservice.domain.UserRole;
import com.itam.userservice.domain.UserRoleRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUserRoleRepository implements UserRoleRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcUserRoleRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserRole create(UUID tenantId, UUID userId, UUID roleId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        jdbcTemplate.update(
            "INSERT INTO user_roles (tenant_id, user_id, role_id) "
                + "VALUES (:tenantId, :userId, :roleId)",
            Map.of("tenantId", scopedTenantId, "userId", userId, "roleId", roleId)
        );
        return new UserRole(scopedTenantId, userId, roleId);
    }

    @Override
    public boolean exists(UUID tenantId, UUID userId, UUID roleId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user_roles "
                + "WHERE tenant_id = :tenantId AND user_id = :userId AND role_id = :roleId",
            Map.of("tenantId", scopedTenantId, "userId", userId, "roleId", roleId),
            Integer.class
        );
        return count != null && count > 0;
    }

    @Override
    public List<String> findRoleNamesByUserId(UUID tenantId, UUID userId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT r.name FROM user_roles ur "
                + "JOIN roles r ON r.tenant_id = ur.tenant_id AND r.id = ur.role_id "
                + "WHERE ur.tenant_id = :tenantId AND ur.user_id = :userId "
                + "ORDER BY r.name, r.id",
            Map.of("tenantId", scopedTenantId, "userId", userId),
            (resultSet, rowNum) -> resultSet.getString("name")
        );
    }
}
