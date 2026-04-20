package com.itam.ticketservice.domain;

import java.time.Instant;
import java.util.UUID;

public record AuditRecord(
    UUID id,
    UUID tenantId,
    String actor,
    AuditEntity entityType,
    UUID entityId,
    AuditAction actionType,
    Instant occurredAt,
    String correlationId,
    String changePayload
) {
}
