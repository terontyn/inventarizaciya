package com.itam.userservice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class SchemaConstraintIntegrationTest extends IntegrationTestSupport {

    @Test
    void migrationsEnforceTenantAndSameTenantConstraintsAcrossFoundationTables() {
        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO departments (id, name) VALUES (?, ?)",
                UUID.randomUUID(),
                "Finance"
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO users (id, name) VALUES (?, ?)",
                UUID.randomUUID(),
                "Alice"
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        UUID tenantA = insertTenant("Tenant A");
        UUID tenantB = insertTenant("Tenant B");
        UUID departmentA = insertDepartment(tenantA, "Finance");
        UUID foreignBranchId = insertBranch(tenantB, "Foreign Branch");
        UUID foreignDepartmentId = insertDepartment(tenantB, "Foreign Department");
        UUID userId = insertUser(tenantA, "Alice", departmentA);
        UUID roleId = insertRole(tenantA, "Manager");
        UUID foreignRoleId = insertRole(tenantB, "Foreign Role");

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO locations (id, tenant_id, branch_id, name) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(),
                tenantA,
                foreignBranchId,
                "Room 101"
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO users (id, tenant_id, department_id, name) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(),
                tenantA,
                foreignDepartmentId,
                "Bob"
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        insertUserRole(tenantA, userId, roleId);

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO user_roles (tenant_id, user_id, role_id) VALUES (?, ?, ?)",
                tenantA,
                userId,
                roleId
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO user_roles (tenant_id, user_id, role_id) VALUES (?, ?, ?)",
                tenantA,
                userId,
                foreignRoleId
            )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
