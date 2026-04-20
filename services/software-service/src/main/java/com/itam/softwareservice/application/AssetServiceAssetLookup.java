package com.itam.softwareservice.application;

import java.util.UUID;

public interface AssetServiceAssetLookup {

    boolean assetExists(UUID tenantId, UUID authenticatedUserId, UUID assetId);
}
