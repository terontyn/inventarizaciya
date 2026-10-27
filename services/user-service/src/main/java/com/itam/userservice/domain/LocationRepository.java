package com.itam.userservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationRepository {

    Location create(UUID tenantId, String name, UUID branchId);

    List<Location> findAll(UUID tenantId);

    Optional<Location> findById(UUID tenantId, UUID id);

    Optional<Location> update(UUID tenantId, UUID id, String name, UUID branchId);
}
