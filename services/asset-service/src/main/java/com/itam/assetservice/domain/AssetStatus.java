package com.itam.assetservice.domain;

import java.util.Set;

public final class AssetStatus {

    public static final String ACTIVE = "ACTIVE";
    public static final String RETIRED = "RETIRED";
    public static final Set<String> CANONICAL_VALUES = Set.of(ACTIVE, RETIRED);

    private AssetStatus() {
    }
}
