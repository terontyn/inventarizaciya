package com.itam.userservice.application;

import com.itam.userservice.domain.UserRoleRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PilotAuthorizationService {

    private final UserRoleRepository userRoleRepository;

    public PilotAuthorizationService(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    public void requireTenantAdmin(UUID tenantId, UUID authenticatedUserId) {
        requireAnyRole(tenantId, authenticatedUserId, PilotRoleNames.TENANT_ADMIN_ONLY);
    }

    public void requireReferenceRead(UUID tenantId, UUID authenticatedUserId) {
        requireAnyRole(tenantId, authenticatedUserId, PilotRoleNames.PILOT_READ_ROLES);
    }

    public List<String> findRoleNames(UUID tenantId, UUID userId) {
        return userRoleRepository.findRoleNamesByUserId(tenantId, userId);
    }

    public void requireAnyRole(UUID tenantId, UUID authenticatedUserId, Set<String> allowedRoleNames) {
        List<String> roleNames = findRoleNames(tenantId, authenticatedUserId);
        boolean allowed = roleNames.stream().anyMatch(allowedRoleNames::contains);
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authenticated user lacks required role");
        }
    }
}
