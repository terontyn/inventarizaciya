package com.itam.userservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.itam.userservice.domain.BranchRepository;
import com.itam.userservice.domain.DepartmentRepository;
import com.itam.userservice.domain.LocationRepository;
import com.itam.userservice.infrastructure.SqlTenantScope;
import java.lang.reflect.Method;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TenantScopedRepositoryContractTest {

    @Test
    void tenantScopedRepositoryMethodsRequireTenantIdAsFirstParameter() {
        assertTenantScopeFirstParameter(DepartmentRepository.class);
        assertTenantScopeFirstParameter(BranchRepository.class);
        assertTenantScopeFirstParameter(LocationRepository.class);
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
}
