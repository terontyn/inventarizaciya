package com.itam.assetservice.domain;

import java.util.List;
import java.util.UUID;

public interface AssetResponsibilityFactRepository {

    void append(UUID tenantId, AssetResponsibilityFact fact);

    List<AssetResponsibilityFact> findByAssetId(UUID tenantId, UUID assetId);
}
