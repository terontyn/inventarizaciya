package com.itam.assetservice.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public record AssetTechnicalAttributes(
    UUID assetId,
    AssetTechnicalAttributeProfile profile,
    Map<String, String> values
) {

    public AssetTechnicalAttributes {
        values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }
}
