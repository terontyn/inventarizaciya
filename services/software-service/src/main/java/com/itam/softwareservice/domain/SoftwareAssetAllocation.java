package com.itam.softwareservice.domain;

import java.util.UUID;

public record SoftwareAssetAllocation(UUID id, UUID tenantId, UUID softwareRecordId, UUID assetId) {
}
