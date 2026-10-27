package com.itam.reportingservice.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetServiceAssetLookup {

    List<AssetSummary> findAllAssets(UUID tenantId, UUID authenticatedUserId);

    Optional<AssetSummary> findAsset(UUID tenantId, UUID authenticatedUserId, UUID assetId);

    record AssetSummary(UUID assetId, UUID assignedUserId, UUID assignedLocationId) {
    }
}
