package com.itam.assetservice.domain;

import java.util.List;

public enum AssetTechnicalAttributeProfile {
    PC_LAPTOP(List.of(
        AssetTechnicalAttributeKey.HOSTNAME,
        AssetTechnicalAttributeKey.ANYDESK_ID,
        AssetTechnicalAttributeKey.IP_ADDRESS,
        AssetTechnicalAttributeKey.MAC_ADDRESS
    )),
    STATIONARY_PHONE(List.of(
        AssetTechnicalAttributeKey.PHONE_IP,
        AssetTechnicalAttributeKey.EXTENSION_NUMBER,
        AssetTechnicalAttributeKey.SIP_LINE_ID
    )),
    PRINTER(List.of(
        AssetTechnicalAttributeKey.PRINTER_IP,
        AssetTechnicalAttributeKey.PRINTER_NETWORK_NAME,
        AssetTechnicalAttributeKey.PRINTER_PLACEMENT_NOTE
    )),
    UNSUPPORTED_GENERIC(List.of());

    private final List<AssetTechnicalAttributeKey> allowedKeys;

    AssetTechnicalAttributeProfile(List<AssetTechnicalAttributeKey> allowedKeys) {
        this.allowedKeys = List.copyOf(allowedKeys);
    }

    public List<AssetTechnicalAttributeKey> allowedKeys() {
        return allowedKeys;
    }

    public boolean allows(AssetTechnicalAttributeKey key) {
        return allowedKeys.contains(key);
    }
}
