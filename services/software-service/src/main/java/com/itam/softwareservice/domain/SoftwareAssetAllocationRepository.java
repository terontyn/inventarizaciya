package com.itam.softwareservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SoftwareAssetAllocationRepository {

    List<SoftwareAssetAllocation> findBySoftwareRecordId(UUID tenantId, UUID recordId);

    List<SoftwareAssetAllocation> findByAssetId(UUID tenantId, UUID assetId);

    Optional<SoftwareAssetAllocation> findBySoftwareRecordIdAndAssetId(UUID tenantId, UUID recordId, UUID assetId);

    SoftwareAssetAllocation create(UUID tenantId, UUID recordId, UUID assetId);

    void deleteById(UUID tenantId, UUID allocationId);
}
