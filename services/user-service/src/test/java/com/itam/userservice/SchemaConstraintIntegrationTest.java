package com.itam.userservice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class SchemaConstraintIntegrationTest extends IntegrationTestSupport {

    @Test
    void migrationsEnforceNotNullTenantAndSameTenantLocationBranchConstraint() {
        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO departments (id, name) VALUES (?, ?)",
                UUID.randomUUID(),
                "Finance"
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        UUID tenantA = insertTenant("Tenant A");
        UUID tenantB = insertTenant("Tenant B");
        UUID foreignBranchId = insertBranch(tenantB, "Foreign Branch");

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO locations (id, tenant_id, branch_id, name) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(),
                tenantA,
                foreignBranchId,
                "Room 101"
            )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
