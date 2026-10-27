package com.itam.softwareservice.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SoftwareRecordRepository {

    List<SoftwareRecord> findAll(UUID tenantId);

    Optional<SoftwareRecord> findById(UUID tenantId, UUID recordId);

    List<SoftwareRecord> findByIds(UUID tenantId, List<UUID> recordIds);

    SoftwareRecord create(UUID tenantId, String name);
}
