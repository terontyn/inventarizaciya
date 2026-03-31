package com.itam.assetservice.domain;

import java.util.UUID;

public record Asset(
    UUID id,
    UUID tenantId,
    String assetTag,
    String serialNumber,
    String assetType,
    String status,
    UUID assignedUserId,
    UUID locationId
) {
}
