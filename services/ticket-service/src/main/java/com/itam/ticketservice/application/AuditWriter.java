package com.itam.ticketservice.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itam.ticketservice.domain.AuditAction;
import com.itam.ticketservice.domain.AuditEntity;
import com.itam.ticketservice.domain.AuditRecord;
import com.itam.ticketservice.domain.AuditRecordRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditWriter {

    private final AuditRecordRepository auditRecordRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public AuditWriter(
        AuditRecordRepository auditRecordRepository,
        ObjectMapper objectMapper,
        Clock clock
    ) {
        this.auditRecordRepository = auditRecordRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public void record(
        UUID tenantId,
        AuditMetadata auditMetadata,
        AuditEntity entityType,
        UUID entityId,
        AuditAction actionType,
        Object changePayload
    ) {
        auditRecordRepository.append(new AuditRecord(
            UUID.randomUUID(),
            tenantId,
            auditMetadata.actor(),
            entityType,
            entityId,
            actionType,
            Instant.now(clock),
            auditMetadata.correlationId(),
            serialize(changePayload)
        ));
    }

    private String serialize(Object changePayload) {
        try {
            return objectMapper.writeValueAsString(changePayload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize audit payload", exception);
        }
    }
}
