package com.itam.userservice.domain;

import java.util.UUID;

public record User(UUID id, UUID tenantId, String name, UUID departmentId) {
}
