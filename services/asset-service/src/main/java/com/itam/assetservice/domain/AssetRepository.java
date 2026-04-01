package com.itam.assetservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository {

    Asset create(
        UUID tenantId,
        String assetTag,
        String serialNumber,
        String assetType,
        String status,
        UUID assignedUserId,
        UUID locationId
    );

    List<Asset> findAll(UUID tenantId);

    Optional<Asset> findById(UUID tenantId, UUID id);

    Optional<Asset> update(
        UUID tenantId,
        UUID id,
        String assetTag,
        String serialNumber,
        String assetType,
        String status
    );

    Optional<Asset> updateCurrentState(
        UUID tenantId,
        UUID id,
        UUID assignedUserId,
        UUID locationId
    );
}
