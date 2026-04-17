package com.itam.maintenanceservice.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceWorkRepository {

    List<MaintenanceWork> findOpen(UUID tenantId);

    Optional<MaintenanceWork> findById(UUID tenantId, UUID workId);

    Optional<MaintenanceWork> findOpenByAssetId(UUID tenantId, UUID assetId);

    MaintenanceWork createOpen(UUID tenantId, UUID assetId, MaintenanceWorkType workType, Instant openedAt);

    Optional<MaintenanceWork> complete(UUID tenantId, UUID workId, Instant completedAt);
}
