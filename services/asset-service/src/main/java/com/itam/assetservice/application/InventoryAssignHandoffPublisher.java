package com.itam.assetservice.application;

import java.util.UUID;

public interface InventoryAssignHandoffPublisher {

    void publishAfterAssign(UUID tenantId, UUID authenticatedUserId, UUID assetId, AuditMetadata auditMetadata);
}
