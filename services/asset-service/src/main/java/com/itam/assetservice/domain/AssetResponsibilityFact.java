package com.itam.assetservice.domain;

import java.time.Instant;
import java.util.UUID;

public record AssetResponsibilityFact(
    UUID id,
    UUID tenantId,
    UUID assetId,
    AssetResponsibilityFactType factType,
    UUID previousAssignedUserId,
    UUID newAssignedUserId,
    UUID previousLocationId,
    UUID newLocationId,
    Instant occurredAt
) {
}
