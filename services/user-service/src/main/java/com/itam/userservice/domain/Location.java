package com.itam.userservice.domain;

import java.util.UUID;

public record Location(UUID id, UUID tenantId, UUID branchId, String name) {
}
