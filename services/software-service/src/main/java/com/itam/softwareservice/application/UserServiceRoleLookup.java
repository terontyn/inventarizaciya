package com.itam.softwareservice.application;

import java.util.Set;
import java.util.UUID;

public interface UserServiceRoleLookup {

    Set<String> findRoleNames(UUID tenantId, UUID authenticatedUserId);
}
