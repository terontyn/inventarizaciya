package com.itam.reportingservice.application;

import com.itam.reportingservice.domain.SoftwareRelationSummary;
import java.util.List;
import java.util.UUID;

public interface SoftwareServiceSoftwareLookup {

    DerivedSliceResult<SoftwareSlice> findAssetSoftware(UUID tenantId, UUID authenticatedUserId, UUID assetId);

    record SoftwareSlice(UUID assetId, List<SoftwareRelationSummary> softwareRelations) {
    }
}
