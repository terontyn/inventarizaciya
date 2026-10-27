package com.itam.userservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchRepository {

    Branch create(UUID tenantId, String name);

    List<Branch> findAll(UUID tenantId);

    Optional<Branch> findById(UUID tenantId, UUID id);

    Optional<Branch> update(UUID tenantId, UUID id, String name);

    boolean exists(UUID tenantId, UUID id);
}
