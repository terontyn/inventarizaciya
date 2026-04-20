package com.itam.softwareservice.infrastructure;

import com.itam.softwareservice.domain.AuditAction;
import com.itam.softwareservice.domain.AuditEntity;
import com.itam.softwareservice.domain.AuditRecord;
import com.itam.softwareservice.domain.AuditRecordRepository;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    @Override
    public List<AuditRecord> findByEntity(UUID tenantId, AuditEntity entityType, UUID entityId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, actor, entity_type, entity_id, action_type, occurred_at, correlation_id, change_payload "
                + "FROM local_audit_records "
                + "WHERE tenant_id = :tenantId AND entity_type = :entityType AND entity_id = :entityId "
                + "ORDER BY occurred_at DESC, id DESC",
            Map.of(
                "tenantId", scopedTenantId,
                "entityType", entityType.name(),
                "entityId", entityId
            ),
            (resultSet, rowNum) -> new AuditRecord(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("tenant_id", UUID.class),
                resultSet.getString("actor"),
                AuditEntity.valueOf(resultSet.getString("entity_type")),
                resultSet.getObject("entity_id", UUID.class),
                AuditAction.valueOf(resultSet.getString("action_type")),
                resultSet.getTimestamp("occurred_at").toInstant(),
                resultSet.getString("correlation_id"),
                resultSet.getString("change_payload")
            )
        );
    }
}
