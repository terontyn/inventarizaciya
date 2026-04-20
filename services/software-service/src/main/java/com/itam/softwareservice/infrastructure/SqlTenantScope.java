package com.itam.softwareservice.infrastructure;

import java.util.UUID;

public final class SqlTenantScope {

    private SqlTenantScope() {
    }

    public static UUID requireTenantId(UUID tenantId) {
        if (tenantId == null) {
            throw new NullPointerException("tenantId is required");
        }
        return tenantId;
    }
}
