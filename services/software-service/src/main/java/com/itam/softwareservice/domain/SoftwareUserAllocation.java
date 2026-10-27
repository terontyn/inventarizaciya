package com.itam.softwareservice.domain;

import java.util.UUID;

public record SoftwareUserAllocation(UUID id, UUID tenantId, UUID softwareRecordId, UUID userId) {
}
