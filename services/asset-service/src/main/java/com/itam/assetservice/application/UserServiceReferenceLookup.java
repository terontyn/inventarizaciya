package com.itam.assetservice.application;

import java.util.UUID;

public interface UserServiceReferenceLookup {

    boolean userExists(UUID tenantId, UUID userId);

    boolean locationExists(UUID tenantId, UUID locationId);
}
