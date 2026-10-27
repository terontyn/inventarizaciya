package com.itam.reportingservice.application;

import java.util.UUID;

public interface InventoryServiceInventoryLookup {

    DerivedSliceResult<InventorySlice> findAssetInventory(UUID tenantId, UUID authenticatedUserId, UUID assetId);

    record InventorySlice(
        Boolean inStorageNow,
        UUID warehouseId,
        UUID storagePlaceId,
        String readinessState
    ) {
    }
}
