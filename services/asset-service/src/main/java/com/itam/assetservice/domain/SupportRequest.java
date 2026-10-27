package com.itam.assetservice.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SupportRequest(
    UUID id,
    UUID tenantId,
    UUID userId,
    String userName,
    String type,
    String description,
    SupportRequestStatus status,
    String resolutionComment,
    OffsetDateTime createdAt
) {
    public enum SupportRequestStatus {
        NEW, APPROVED, REJECTED, NEEDS_WORK, RESOLVED
    }
}
