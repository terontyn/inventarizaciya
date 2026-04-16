package com.itam.inventoryservice.domain;

import java.util.UUID;

public record InventoryRecord(
    UUID id,
    UUID tenantId,
    UUID assetId,
    UUID warehouseId,
    UUID storagePlaceId
) {
}
