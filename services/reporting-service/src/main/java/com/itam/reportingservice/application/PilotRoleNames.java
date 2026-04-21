package com.itam.reportingservice.application;

import java.util.Set;

public final class PilotRoleNames {

    public static final String TENANT_ADMIN = "Tenant Admin";
    public static final String ASSET_OPERATOR = "Asset Operator";
    public static final String SUPPORT_REVIEWER = "Support Reviewer";

    public static final Set<String> REPORTING_READ_ROLES = Set.of(
        TENANT_ADMIN,
        ASSET_OPERATOR,
        SUPPORT_REVIEWER
    );

    private PilotRoleNames() {
    }
}
