package com.itam.reportingservice.application;

import java.util.UUID;

public interface MaintenanceServiceMaintenanceLookup {

    DerivedSliceResult<MaintenanceSlice> findAssetMaintenance(UUID tenantId, UUID authenticatedUserId, UUID assetId);

    record MaintenanceSlice(UUID assetId, Boolean hasOpenMaintenanceWork) {
    }
}
