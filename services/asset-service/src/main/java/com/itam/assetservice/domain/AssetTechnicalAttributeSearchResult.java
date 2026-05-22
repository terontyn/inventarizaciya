package com.itam.assetservice.domain;

import java.util.UUID;

public record AssetTechnicalAttributeSearchResult(
    UUID assetId,
    String assetTag,
    String displayName,
    String assetType,
    String serialNumber,
    String status,
    AssetTechnicalAttributeKey matchedKey,
    String matchedValue
) {
}
