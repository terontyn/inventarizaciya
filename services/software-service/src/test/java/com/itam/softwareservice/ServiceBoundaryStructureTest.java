package com.itam.softwareservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.itam.softwareservice.api.SoftwareController;
import com.itam.softwareservice.application.AuditWriter;
import com.itam.softwareservice.application.SoftwareApplicationService;
import com.itam.softwareservice.application.SoftwareAuthorizationService;
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
            SoftwareController.class,
            SoftwareApplicationService.class,
            SoftwareAuthorizationService.class,
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
