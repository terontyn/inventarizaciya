package com.itam.assetservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupportRequestRepository {
    SupportRequest save(SupportRequest request);
    List<SupportRequest> findByTenantId(UUID tenantId);
    Optional<SupportRequest> findById(UUID tenantId, UUID id);
}
