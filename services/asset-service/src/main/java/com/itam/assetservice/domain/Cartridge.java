package com.itam.assetservice.domain;

import java.util.UUID;

public record Cartridge(
    UUID id,
    UUID tenantId,
    String model,
    String compatibility,
    UUID locationId,
    String status,
    UUID printerAssetId
) {
}
