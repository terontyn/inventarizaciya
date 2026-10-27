package com.itam.softwareservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SoftwareUserAllocationRepository {

    List<SoftwareUserAllocation> findBySoftwareRecordId(UUID tenantId, UUID recordId);

    List<SoftwareUserAllocation> findByUserId(UUID tenantId, UUID userId);

    Optional<SoftwareUserAllocation> findBySoftwareRecordIdAndUserId(UUID tenantId, UUID recordId, UUID userId);

    SoftwareUserAllocation create(UUID tenantId, UUID recordId, UUID userId);

    void deleteById(UUID tenantId, UUID allocationId);
}
