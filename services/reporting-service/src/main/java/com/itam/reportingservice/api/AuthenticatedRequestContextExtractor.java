package com.itam.reportingservice.api;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthenticatedRequestContextExtractor {

    public static final String AUTHENTICATED_TENANT_HEADER = "X-Authenticated-Tenant-Id";
    public static final String AUTHENTICATED_USER_HEADER = "X-Authenticated-User-Id";

    public UUID requireTenantId(HttpServletRequest request) {
        return parseUuidHeader(request, AUTHENTICATED_TENANT_HEADER);
    }

    public UUID requireAuthenticatedUserId(HttpServletRequest request) {
        return parseUuidHeader(request, AUTHENTICATED_USER_HEADER);
    }

    private UUID parseUuidHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, headerName + " header is required");
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, headerName + " must be a valid UUID");
        }
    }
}
