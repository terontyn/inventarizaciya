package com.itam.userservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.itam.userservice.api.BranchController;
import com.itam.userservice.api.DepartmentController;
import com.itam.userservice.api.LocationController;
import com.itam.userservice.api.RoleController;
import com.itam.userservice.api.TenantController;
import com.itam.userservice.api.UserController;
import com.itam.userservice.application.AuditWriter;
import com.itam.userservice.application.BranchApplicationService;
import com.itam.userservice.application.DepartmentApplicationService;
import com.itam.userservice.application.LocationApplicationService;
import com.itam.userservice.application.RoleApplicationService;
import com.itam.userservice.application.TenantApplicationService;
import com.itam.userservice.application.UserApplicationService;
import com.itam.userservice.application.UserRoleApplicationService;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class ServiceBoundaryStructureTest {

    @Test
    void apiAndApplicationLayersDoNotDependOnJdbcTemplatesDirectly() {
        List<Class<?>> boundaryClasses = List.of(
            TenantController.class,
            DepartmentController.class,
            BranchController.class,
            LocationController.class,
            UserController.class,
            RoleController.class,
            TenantApplicationService.class,
            DepartmentApplicationService.class,
            BranchApplicationService.class,
            LocationApplicationService.class,
            UserApplicationService.class,
            RoleApplicationService.class,
            UserRoleApplicationService.class,
            AuditWriter.class
        );

        for (Class<?> boundaryClass : boundaryClasses) {
            assertThat(directFieldTypes(boundaryClass))
                .doesNotContain(JdbcTemplate.class, NamedParameterJdbcTemplate.class);
            assertThat(constructorParameterTypes(boundaryClass))
                .doesNotContain(JdbcTemplate.class, NamedParameterJdbcTemplate.class);
        }
    }

    private List<Class<?>> directFieldTypes(Class<?> boundaryClass) {
        return java.util.Arrays.stream(boundaryClass.getDeclaredFields())
            .map(Field::getType)
            .toList();
    }

    private List<Class<?>> constructorParameterTypes(Class<?> boundaryClass) {
        return java.util.Arrays.stream(boundaryClass.getDeclaredConstructors())
            .map(Constructor::getParameterTypes)
            .flatMap(java.util.Arrays::stream)
            .toList();
    }
}
