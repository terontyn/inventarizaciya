package com.itam.assetservice.domain;

import java.util.UUID;

public interface AssetResponsibilityFactRepository {

    void append(UUID tenantId, AssetResponsibilityFact fact);
}
