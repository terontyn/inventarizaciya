package com.itam.inventoryservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository {

    List<Warehouse> findAll(UUID tenantId);

    Optional<Warehouse> findById(UUID tenantId, UUID id);
}
