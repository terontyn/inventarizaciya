package com.itam.assetservice.application;

import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssetAuthorizationService {

    private final UserServiceRoleLookup userServiceRoleLookup;

    public AssetAuthorizationService(UserServiceRoleLookup userServiceRoleLookup) {
        this.userServiceRoleLookup = userServiceRoleLookup;
    }

    public void requireAssetReadAccess(UUID tenantId, UUID authenticatedUserId) {
        requireAnyRole(tenantId, authenticatedUserId, PilotRoleNames.ASSET_READ_ROLES);
    }

    public void requireAssetMutateAccess(UUID tenantId, UUID authenticatedUserId) {
        requireAnyRole(tenantId, authenticatedUserId, PilotRoleNames.ASSET_MUTATE_ROLES);
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
