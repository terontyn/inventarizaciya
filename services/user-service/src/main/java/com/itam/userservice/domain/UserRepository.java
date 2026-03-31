package com.itam.userservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User create(UUID tenantId, String name, UUID departmentId);

    List<User> findAll(UUID tenantId);

    Optional<User> findById(UUID tenantId, UUID id);

    Optional<User> update(UUID tenantId, UUID id, String name, UUID departmentId);

    boolean exists(UUID tenantId, UUID id);
}
