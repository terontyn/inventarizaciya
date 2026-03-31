package com.itam.userservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.itam.userservice.application.AuditMetadata;
import com.itam.userservice.application.UserRoleApplicationService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class UserRoleApplicationIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private UserRoleApplicationService userRoleApplicationService;

    @Test
    void bindUserRolePersistsInsideTenantAndWritesAudit() {
        UUID tenantId = insertTenant("Tenant A");
        UUID userId = insertUser(tenantId, "Alice", null);
        UUID roleId = insertRole(tenantId, "Manager");

        userRoleApplicationService.bind(
            tenantId,
            userId,
            roleId,
            new AuditMetadata("role-admin", "corr-user-role-create")
        );

        assertThat(countRows("user_roles")).isEqualTo(1);
        AuditRow auditRow = latestAuditRow();
        assertThat(auditRow.entityType()).isEqualTo("USER_ROLE");
        assertThat(auditRow.actionType()).isEqualTo("CREATE");
        assertThat(auditRow.correlationId()).isEqualTo("corr-user-role-create");
        assertThat(auditRow.changePayload()).contains("\"user_id\":\"" + userId + "\"");
        assertThat(auditRow.changePayload()).contains("\"role_id\":\"" + roleId + "\"");
    }

    @Test
    void bindUserRoleRejectsCrossTenantRoleReference() {
        UUID tenantA = insertTenant("Tenant A");
        UUID tenantB = insertTenant("Tenant B");
        UUID userId = insertUser(tenantA, "Alice", null);
        UUID foreignRoleId = insertRole(tenantB, "Foreign Role");

        assertThatThrownBy(() ->
            userRoleApplicationService.bind(
                tenantA,
                userId,
                foreignRoleId,
                new AuditMetadata("role-admin", "corr-user-role-cross-tenant")
            )
        )
            .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            );

        assertThat(countRows("user_roles")).isEqualTo(0);
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void bindUserRoleRejectsDuplicateBinding() {
        UUID tenantId = insertTenant("Tenant A");
        UUID userId = insertUser(tenantId, "Alice", null);
        UUID roleId = insertRole(tenantId, "Manager");

        userRoleApplicationService.bind(
            tenantId,
            userId,
            roleId,
            new AuditMetadata("role-admin", "corr-user-role-first")
        );

        assertThatThrownBy(() ->
            userRoleApplicationService.bind(
                tenantId,
                userId,
                roleId,
                new AuditMetadata("role-admin", "corr-user-role-duplicate")
            )
        )
            .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT)
            );

        assertThat(countRows("user_roles")).isEqualTo(1);
        assertThat(countRows("local_audit_records")).isEqualTo(1);
    }
}
