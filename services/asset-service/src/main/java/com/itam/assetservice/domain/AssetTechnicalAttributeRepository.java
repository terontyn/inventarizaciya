package com.itam.assetservice.domain;

import java.util.Map;
import java.util.UUID;

public interface AssetTechnicalAttributeRepository {

    Map<String, String> findByAssetId(UUID tenantId, UUID assetId);

    void replaceForAsset(UUID tenantId, UUID assetId, Map<String, String> values);
}
