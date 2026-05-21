package com.itam.assetservice.domain;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum AssetTechnicalAttributeKey {
    HOSTNAME("hostname"),
    ANYDESK_ID("anydesk_id"),
    IP_ADDRESS("ip_address"),
    MAC_ADDRESS("mac_address"),
    PHONE_IP("phone_ip"),
    EXTENSION_NUMBER("extension_number"),
    SIP_LINE_ID("sip_line_id"),
    PRINTER_IP("printer_ip"),
    PRINTER_NETWORK_NAME("printer_network_name"),
    PRINTER_PLACEMENT_NOTE("printer_placement_note");

    private final String key;

    AssetTechnicalAttributeKey(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public static Optional<AssetTechnicalAttributeKey> fromKey(String rawKey) {
        if (rawKey == null) {
            return Optional.empty();
        }
        String normalizedKey = rawKey.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
            .filter(key -> key.key.equals(normalizedKey))
            .findFirst();
    }
}
