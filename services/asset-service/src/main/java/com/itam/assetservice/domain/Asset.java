package com.itam.assetservice.domain;

import java.time.Instant;
import java.util.UUID;

public record Asset(
    UUID id,
    UUID tenantId,
    String assetTag,
    String serialNumber,
    String assetType,
    String status,
    UUID assignedUserId,
    UUID locationId,
    String displayName,
    String note,
    Instant archivedAt
) {
}
