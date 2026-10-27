package com.itam.assetservice.domain;

import java.util.UUID;

public record AssetType(
    UUID id,
    UUID tenantId,
    String name,
    String attributes
) {
}
