package com.itam.assetservice.domain;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AssetTechnicalAttributeRepository {

    Map<String, String> findByAssetId(UUID tenantId, UUID assetId);

    void replaceForAsset(UUID tenantId, UUID assetId, Map<String, String> values);

    List<AssetTechnicalAttributeSearchResult> searchByTechnicalAttributes(
        UUID tenantId,
        String normalizedQuery,
        List<AssetTechnicalAttributeKey> searchableKeys,
        int limit
    );

    List<AssetEmployeeSheetTechnicalHintRow> findEmployeeSheetTechnicalHints(
        UUID tenantId,
        UUID assignedUserId,
        List<AssetTechnicalAttributeKey> allowedHintKeys
    );
}
