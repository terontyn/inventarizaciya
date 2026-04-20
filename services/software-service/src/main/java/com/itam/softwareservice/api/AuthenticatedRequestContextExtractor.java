package com.itam.softwareservice.api;

import com.itam.softwareservice.application.AuditMetadata;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthenticatedRequestContextExtractor {

    public static final String AUTHENTICATED_TENANT_HEADER = "X-Authenticated-Tenant-Id";
    public static final String AUTHENTICATED_ACTOR_HEADER = "X-Authenticated-Actor";
    public static final String AUTHENTICATED_USER_HEADER = "X-Authenticated-User-Id";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    public UUID requireTenantId(HttpServletRequest request) {
        return parseTenantId(request, true)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                AUTHENTICATED_TENANT_HEADER + " header is required"
            ));
    }

    public Optional<UUID> findTenantId(HttpServletRequest request) {
        return parseTenantId(request, false);
    }

    public UUID requireAuthenticatedUserId(HttpServletRequest request) {
        String rawUserId = requiredHeader(request, AUTHENTICATED_USER_HEADER);
        try {
            return UUID.fromString(rawUserId);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                AUTHENTICATED_USER_HEADER + " must be a valid UUID"
            );
        }
    }

    public AuditMetadata requireAuditMetadata(HttpServletRequest request) {
        String actor = requiredHeader(request, AUTHENTICATED_ACTOR_HEADER);
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        String resolvedCorrelationId = correlationId == null || correlationId.trim().isEmpty()
            ? UUID.randomUUID().toString()
            : correlationId.trim();
        return new AuditMetadata(actor, resolvedCorrelationId);
    }

    private Optional<UUID> parseTenantId(HttpServletRequest request, boolean required) {
        String rawTenantId = request.getHeader(AUTHENTICATED_TENANT_HEADER);
        if (rawTenantId == null || rawTenantId.trim().isEmpty()) {
            if (required) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    AUTHENTICATED_TENANT_HEADER + " header is required"
                );
            }
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(rawTenantId.trim()));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                AUTHENTICATED_TENANT_HEADER + " must be a valid UUID"
            );
        }
    }

    private String requiredHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                headerName + " header is required"
            );
        }
        return value.trim();
    }
}
