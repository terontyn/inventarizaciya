package com.itam.softwareservice.domain;

import java.util.UUID;

public record SoftwareRecord(UUID id, UUID tenantId, String name) {
}
