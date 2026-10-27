package com.itam.userservice.domain;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository {

    UserRole create(UUID tenantId, UUID userId, UUID roleId);

    boolean exists(UUID tenantId, UUID userId, UUID roleId);

    List<String> findRoleNamesByUserId(UUID tenantId, UUID userId);

    void deleteByUserId(UUID tenantId, UUID userId);
}
