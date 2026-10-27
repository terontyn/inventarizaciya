package com.itam.assetservice.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository {

    Asset create(
        UUID tenantId,
        UUID id,
        String assetTag,
        String serialNumber,
        String assetType,
        UUID assignedUserId,
        UUID locationId,
        String displayName,
        String note,
        String qrData,
        UUID parentAssetId,
        String ipAddress
    );

    List<Asset> findAll(UUID tenantId);

    List<Asset> findArchived(UUID tenantId);

    Optional<Asset> findById(UUID tenantId, UUID id);

    Optional<Asset> findByIdForUpdate(UUID tenantId, UUID id);

    Optional<Asset> update(
        UUID tenantId,
        UUID id,
        String assetTag,
        String serialNumber,
        String assetType,
        String displayName,
        String note
    );

    Optional<Asset> updateCurrentState(
        UUID tenantId,
        UUID id,
        String status,
        UUID assignedUserId,
        UUID locationId
    );

    Optional<Asset> archive(UUID tenantId, UUID id, Instant archivedAt);

    Optional<Asset> unarchive(UUID tenantId, UUID id);
}
