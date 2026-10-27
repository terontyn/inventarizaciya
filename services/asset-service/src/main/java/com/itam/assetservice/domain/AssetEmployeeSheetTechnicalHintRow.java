package com.itam.assetservice.domain;

import java.util.UUID;

public record AssetEmployeeSheetTechnicalHintRow(
    UUID assetId,
    String assetTag,
    String displayName,
    String assetType,
    String serialNumber,
    String status,
    UUID locationId,
    AssetTechnicalAttributeKey hintKey,
    String hintValue
) {
}
