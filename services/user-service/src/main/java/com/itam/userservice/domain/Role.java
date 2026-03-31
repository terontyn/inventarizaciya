package com.itam.userservice.domain;

import java.util.UUID;

public record Role(UUID id, UUID tenantId, String name) {
}
