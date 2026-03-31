package com.itam.assetservice.domain;

public interface AuditRecordRepository {

    void append(AuditRecord auditRecord);
}
