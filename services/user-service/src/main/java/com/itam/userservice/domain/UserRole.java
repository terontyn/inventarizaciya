package com.itam.userservice.domain;

import java.util.UUID;

public record UserRole(UUID tenantId, UUID userId, UUID roleId) {
}
