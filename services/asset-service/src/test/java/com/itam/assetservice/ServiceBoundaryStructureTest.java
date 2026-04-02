package com.itam.assetservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.itam.assetservice.api.AssetController;
import com.itam.assetservice.application.AssetApplicationService;
import com.itam.assetservice.application.AssetAuthorizationService;
import com.itam.assetservice.application.AssetReferenceValidator;
import com.itam.assetservice.application.AssetReviewReadService;
import com.itam.assetservice.application.AssetResponsibilityApplicationService;
import com.itam.assetservice.application.AuditWriter;
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
            AssetController.class,
            AssetApplicationService.class,
            AssetAuthorizationService.class,
            AssetReviewReadService.class,
            AssetResponsibilityApplicationService.class,
            AssetReferenceValidator.class,
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
