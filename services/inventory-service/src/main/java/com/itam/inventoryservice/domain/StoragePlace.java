package com.itam.inventoryservice.domain;

import java.util.UUID;

public record StoragePlace(UUID id, UUID tenantId, UUID warehouseId) {
}
