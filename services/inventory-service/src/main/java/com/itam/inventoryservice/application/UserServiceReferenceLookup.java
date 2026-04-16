package com.itam.inventoryservice.application;

import java.util.UUID;

public interface UserServiceReferenceLookup {

    boolean branchExists(UUID tenantId, UUID authenticatedUserId, UUID branchId);

    boolean locationExists(UUID tenantId, UUID authenticatedUserId, UUID locationId);
}
