package com.itam.assetservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UiCorsConfiguration implements WebMvcConfigurer {

    private static final String AUTHENTICATED_TENANT_HEADER = "X-Authenticated-Tenant-Id";
    private static final String AUTHENTICATED_USER_HEADER = "X-Authenticated-User-Id";
    private static final String AUTHENTICATED_ACTOR_HEADER = "X-Authenticated-Actor";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        configure(registry.addMapping("/api/**"));
    }

    private void configure(CorsRegistration registration) {
        registration
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PATCH", "PUT", "OPTIONS", "DELETE")
            .allowedHeaders(
                "Content-Type",
                AUTHENTICATED_TENANT_HEADER,
                AUTHENTICATED_USER_HEADER,
                AUTHENTICATED_ACTOR_HEADER,
                CORRELATION_ID_HEADER
            )
            .exposedHeaders("Location")
            .maxAge(3600);
    }
}
