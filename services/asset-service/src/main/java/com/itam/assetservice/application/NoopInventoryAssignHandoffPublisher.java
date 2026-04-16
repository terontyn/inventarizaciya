package com.itam.assetservice.application;

import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
    prefix = "integration.inventory-service.assign-handoff",
    name = "enabled",
    havingValue = "false",
    matchIfMissing = true
)
public class NoopInventoryAssignHandoffPublisher implements InventoryAssignHandoffPublisher {

    @Override
    public void publishAfterAssign(UUID tenantId, UUID authenticatedUserId, UUID assetId, AuditMetadata auditMetadata) {
    }
}
