package com.itam.inventoryservice.domain;

import java.util.UUID;

public record Warehouse(UUID id, UUID tenantId) {
}
