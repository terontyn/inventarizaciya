package com.itam.reportingservice.application;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReportingAuthorizationService {

    private final UserServiceRoleLookup userServiceRoleLookup;

    public ReportingAuthorizationService(UserServiceRoleLookup userServiceRoleLookup) {
        this.userServiceRoleLookup = userServiceRoleLookup;
    }

    public void requireReportingReadAccess(UUID tenantId, UUID authenticatedUserId) {
        try {
            boolean allowed = userServiceRoleLookup.findRoleNames(tenantId, authenticatedUserId).stream()
                .anyMatch(PilotRoleNames.REPORTING_READ_ROLES::contains);
            if (!allowed) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authenticated user lacks required role");
            }
        } catch (ReferenceLookupUnavailableException exception) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Unable to resolve caller roles via user-service",
                exception
            );
        }
    }
}
