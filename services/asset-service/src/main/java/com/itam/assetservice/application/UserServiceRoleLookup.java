package com.itam.assetservice.application;

import java.util.List;
import java.util.UUID;

public interface UserServiceRoleLookup {

    List<String> findRoleNames(UUID tenantId, UUID userId);
}
