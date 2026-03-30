package com.itam.userservice.domain;

public interface AuditRecordRepository {

    void append(AuditRecord auditRecord);
}
