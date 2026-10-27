package com.itam.softwareservice.application;

import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SoftwareAuthorizationService {

    private final UserServiceRoleLookup userServiceRoleLookup;

    public SoftwareAuthorizationService(UserServiceRoleLookup userServiceRoleLookup) {
        this.userServiceRoleLookup = userServiceRoleLookup;
    }

    public void requireSoftwareReadAccess(UUID tenantId, UUID authenticatedUserId) {
        requireAnyRole(tenantId, authenticatedUserId, PilotRoleNames.SOFTWARE_READ_ROLES);
    }

    public void requireSoftwareMutateAccess(UUID tenantId, UUID authenticatedUserId) {
        requireAnyRole(tenantId, authenticatedUserId, PilotRoleNames.SOFTWARE_MUTATE_ROLES);
    }

    public void requireAnyRole(UUID tenantId, UUID authenticatedUserId, Set<String> allowedRoleNames) {
        try {
            boolean allowed = userServiceRoleLookup.findRoleNames(tenantId, authenticatedUserId).stream()
                .anyMatch(allowedRoleNames::contains);
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
