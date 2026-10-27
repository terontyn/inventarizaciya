package com.itam.userservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {

    Role create(UUID tenantId, String name);

    List<Role> findAll(UUID tenantId);

    Optional<Role> findById(UUID tenantId, UUID id);

    Optional<Role> update(UUID tenantId, UUID id, String name);

    boolean exists(UUID tenantId, UUID id);
}
