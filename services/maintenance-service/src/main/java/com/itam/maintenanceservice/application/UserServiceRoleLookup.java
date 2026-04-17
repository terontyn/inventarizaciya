package com.itam.maintenanceservice.application;

import java.util.Set;
import java.util.UUID;

public interface UserServiceRoleLookup {

    Set<String> findRoleNames(UUID tenantId, UUID authenticatedUserId);
}
