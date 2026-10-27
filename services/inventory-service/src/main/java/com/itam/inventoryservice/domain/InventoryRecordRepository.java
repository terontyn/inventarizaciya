package com.itam.inventoryservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRecordRepository {

    List<InventoryRecord> findAllCurrent(UUID tenantId);

    Optional<InventoryRecord> findByAssetId(UUID tenantId, UUID assetId);

    InventoryRecord create(
        UUID tenantId,
        UUID assetId,
        UUID warehouseId,
        UUID storagePlaceId,
        ReadinessState readinessState
    );

    Optional<InventoryRecord> updatePlacement(UUID tenantId, UUID assetId, UUID warehouseId, UUID storagePlaceId);

    Optional<InventoryRecord> updateReadiness(UUID tenantId, UUID assetId, ReadinessState readinessState);

    void deleteById(UUID tenantId, UUID id);
}
