package com.itam.userservice.domain;

import java.util.UUID;

public record Department(UUID id, UUID tenantId, String name) {
}
