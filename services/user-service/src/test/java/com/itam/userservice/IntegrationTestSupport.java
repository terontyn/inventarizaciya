package com.itam.userservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM local_audit_records");
        jdbcTemplate.update("DELETE FROM locations");
        jdbcTemplate.update("DELETE FROM branches");
        jdbcTemplate.update("DELETE FROM departments");
        jdbcTemplate.update("DELETE FROM tenants");
    }

    protected UUID insertTenant(String name) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO tenants (id, name) VALUES (?, ?)", id, name);
        return id;
    }

    protected UUID insertDepartment(UUID tenantId, String name) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO departments (id, tenant_id, name) VALUES (?, ?, ?)",
            id,
            tenantId,
            name
        );
        return id;
    }

    protected UUID insertBranch(UUID tenantId, String name) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO branches (id, tenant_id, name) VALUES (?, ?, ?)",
            id,
            tenantId,
            name
        );
        return id;
    }

    protected UUID insertLocation(UUID tenantId, UUID branchId, String name) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO locations (id, tenant_id, branch_id, name) VALUES (?, ?, ?, ?)",
            id,
            tenantId,
            branchId,
            name
        );
        return id;
    }

    protected int countRows(String tableName) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
        return count == null ? 0 : count;
    }

    protected AuditRow latestAuditRow() {
        return jdbcTemplate.query(
            "SELECT tenant_id, actor, entity_type, action_type, correlation_id, change_payload "
                + "FROM local_audit_records ORDER BY occurred_at DESC, id DESC LIMIT 1",
            resultSet -> {
                if (!resultSet.next()) {
                    throw new AssertionError("Expected at least one audit record");
                }
                return new AuditRow(
                    resultSet.getObject("tenant_id", UUID.class),
                    resultSet.getString("actor"),
                    resultSet.getString("entity_type"),
                    resultSet.getString("action_type"),
                    resultSet.getString("correlation_id"),
                    resultSet.getString("change_payload")
                );
            }
        );
    }

    protected record AuditRow(
        UUID tenantId,
        String actor,
        String entityType,
        String actionType,
        String correlationId,
        String changePayload
    ) {
    }
}
