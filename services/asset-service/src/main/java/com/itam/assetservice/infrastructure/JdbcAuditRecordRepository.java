package com.itam.assetservice.infrastructure;

import com.itam.assetservice.domain.AuditRecord;
import com.itam.assetservice.domain.AuditRecordRepository;
import java.sql.Timestamp;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcAuditRecordRepository implements AuditRecordRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcAuditRecordRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void append(AuditRecord auditRecord) {
        jdbcTemplate.update(
            "INSERT INTO local_audit_records ("
                + "id, tenant_id, actor, entity_type, entity_id, action_type, occurred_at, correlation_id, change_payload"
                + ") VALUES ("
                + ":id, :tenantId, :actor, :entityType, :entityId, :actionType, :occurredAt, :correlationId, :changePayload"
                + ")",
            Map.of(
                "id", auditRecord.id(),
                "tenantId", auditRecord.tenantId(),
                "actor", auditRecord.actor(),
                "entityType", auditRecord.entityType().name(),
                "entityId", auditRecord.entityId(),
                "actionType", auditRecord.actionType().name(),
                "occurredAt", Timestamp.from(auditRecord.occurredAt()),
                "correlationId", auditRecord.correlationId(),
                "changePayload", auditRecord.changePayload()
            )
        );
    }
}
