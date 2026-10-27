package com.itam.softwareservice.domain;

import java.util.List;
import java.util.UUID;

public interface AuditRecordRepository {

    void append(AuditRecord auditRecord);

    List<AuditRecord> findByEntity(UUID tenantId, AuditEntity entityType, UUID entityId);
}
