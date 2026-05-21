package com.itam.assetservice.domain;

import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AssetTechnicalAttributeProfileResolver {

    private static final Map<String, AssetTechnicalAttributeProfile> PROFILE_BY_ASSET_TYPE = Map.ofEntries(
        Map.entry("pc", AssetTechnicalAttributeProfile.PC_LAPTOP),
        Map.entry("пк", AssetTechnicalAttributeProfile.PC_LAPTOP),
        Map.entry("computer", AssetTechnicalAttributeProfile.PC_LAPTOP),
        Map.entry("desktop", AssetTechnicalAttributeProfile.PC_LAPTOP),
        Map.entry("laptop", AssetTechnicalAttributeProfile.PC_LAPTOP),
        Map.entry("notebook", AssetTechnicalAttributeProfile.PC_LAPTOP),
        Map.entry("ноутбук", AssetTechnicalAttributeProfile.PC_LAPTOP),
        Map.entry("phone", AssetTechnicalAttributeProfile.STATIONARY_PHONE),
        Map.entry("stationary phone", AssetTechnicalAttributeProfile.STATIONARY_PHONE),
        Map.entry("ip phone", AssetTechnicalAttributeProfile.STATIONARY_PHONE),
        Map.entry("телефон", AssetTechnicalAttributeProfile.STATIONARY_PHONE),
        Map.entry("стационарный телефон", AssetTechnicalAttributeProfile.STATIONARY_PHONE),
        Map.entry("printer", AssetTechnicalAttributeProfile.PRINTER),
        Map.entry("mfp", AssetTechnicalAttributeProfile.PRINTER),
        Map.entry("принтер", AssetTechnicalAttributeProfile.PRINTER),
        Map.entry("мфу", AssetTechnicalAttributeProfile.PRINTER)
    );

    public AssetTechnicalAttributeProfile resolve(String assetType) {
        if (assetType == null) {
            return AssetTechnicalAttributeProfile.UNSUPPORTED_GENERIC;
        }
        String normalizedAssetType = assetType.trim().toLowerCase(Locale.ROOT);
        return PROFILE_BY_ASSET_TYPE.getOrDefault(
            normalizedAssetType,
            AssetTechnicalAttributeProfile.UNSUPPORTED_GENERIC
        );
    }
}
