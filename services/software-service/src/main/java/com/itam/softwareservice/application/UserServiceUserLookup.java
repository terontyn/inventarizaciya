package com.itam.softwareservice.application;

import java.util.UUID;

public interface UserServiceUserLookup {

    boolean userExists(UUID tenantId, UUID authenticatedUserId, UUID userId);
}
