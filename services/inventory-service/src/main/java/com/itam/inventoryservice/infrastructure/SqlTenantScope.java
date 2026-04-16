package com.itam.inventoryservice.infrastructure;

import java.util.Objects;
import java.util.UUID;

public final class SqlTenantScope {

    private SqlTenantScope() {
    }

    public static UUID requireTenantId(UUID tenantId) {
        return Objects.requireNonNull(tenantId, "tenantId is required");
    }
}
