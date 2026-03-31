package com.itam.userservice.domain;

import java.util.UUID;

public interface UserRoleRepository {

    UserRole create(UUID tenantId, UUID userId, UUID roleId);

    boolean exists(UUID tenantId, UUID userId, UUID roleId);
}
