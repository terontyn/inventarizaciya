package com.itam.ticketservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.itam.ticketservice.domain.AuditEntity;
import com.itam.ticketservice.domain.AuditRecordRepository;
import com.itam.ticketservice.domain.TicketRepository;
import com.itam.ticketservice.infrastructure.SqlTenantScope;
import java.lang.reflect.Method;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TenantScopedRepositoryContractTest {

    @Test
    void tenantScopedRepositoryMethodsRequireTenantIdAsFirstParameter() {
        assertTenantScopeFirstParameter(TicketRepository.class);
        assertAuditReadMethodScope();
    }

    @Test
    void repositoryScopeGuardRejectsMissingTenantId() {
        assertThatThrownBy(() -> SqlTenantScope.requireTenantId(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("tenantId is required");
    }

    private void assertTenantScopeFirstParameter(Class<?> repositoryType) {
        for (Method method : repositoryType.getDeclaredMethods()) {
            assertThat(method.getParameterCount())
                .as("Method %s should declare a tenant scope", method.getName())
                .isGreaterThan(0);
            assertThat(method.getParameterTypes()[0])
                .as("Method %s should use tenantId as the first parameter", method.getName())
                .isEqualTo(UUID.class);
        }
    }

    private void assertAuditReadMethodScope() {
        try {
            Method method = AuditRecordRepository.class.getDeclaredMethod(
                "findByEntity",
                UUID.class,
                AuditEntity.class,
                UUID.class
            );
            assertThat(method.getParameterTypes()[0])
                .as("Method %s should use tenantId as the first parameter", method.getName())
                .isEqualTo(UUID.class);
        } catch (NoSuchMethodException exception) {
            throw new AssertionError("Expected audit read method to exist", exception);
        }
    }
}
