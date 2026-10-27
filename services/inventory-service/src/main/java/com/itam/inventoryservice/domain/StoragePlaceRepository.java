package com.itam.inventoryservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoragePlaceRepository {

    List<StoragePlace> findByWarehouseId(UUID tenantId, UUID warehouseId);

    Optional<StoragePlace> findById(UUID tenantId, UUID id);
}
