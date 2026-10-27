package com.itam.assetservice.domain;

import java.util.UUID;

public record SimCard(
    UUID id,
    UUID tenantId,
    String phoneNumber,
    String operator,
    String tariff,
    String issuedToName,
    String locationName,
    String status
) {
}
