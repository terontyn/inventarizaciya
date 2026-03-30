package com.itam.userservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository {

    Department create(UUID tenantId, String name);

    List<Department> findAll(UUID tenantId);

    Optional<Department> findById(UUID tenantId, UUID id);

    Optional<Department> update(UUID tenantId, UUID id, String name);
}
