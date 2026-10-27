package com.itam.maintenanceservice.domain;

import java.time.Instant;
import java.util.UUID;

public record MaintenanceWork(
    UUID id,
    UUID tenantId,
    UUID assetId,
    MaintenanceWorkType workType,
    MaintenanceWorkState workState,
    Instant openedAt,
    Instant completedAt
) {
}
