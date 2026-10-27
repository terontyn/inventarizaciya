package com.itam.assetservice.application;

import java.util.UUID;

public interface UserServiceReferenceLookup {

    boolean userExists(UUID tenantId, UUID authenticatedUserId, UUID userId);

    boolean locationExists(UUID tenantId, UUID authenticatedUserId, UUID locationId);
}
