package com.itam.ticketservice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.itam.ticketservice.domain.TicketState;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class SchemaConstraintIntegrationTest extends IntegrationTestSupport {

    @Test
    void migrationsRejectUnsupportedVocabularyAndCompletionConsistency() {
        UUID tenantId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO tickets (id, tenant_id, asset_id, ticket_state, opened_at) VALUES (?, ?, ?, ?, ?)",
                UUID.randomUUID(),
                tenantId,
                assetId,
                "BROKEN",
                Timestamp.from(Instant.now())
            )
        ).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() ->
            jdbcTemplate.update(
                "INSERT INTO tickets (id, tenant_id, asset_id, ticket_state, opened_at, closed_at) VALUES (?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(),
                tenantId,
                assetId,
                TicketState.OPEN.name(),
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now())
            )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void migrationsAllowMultipleOpenTicketsForSameAsset() {
        UUID tenantId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        insertTicket(tenantId, assetId, TicketState.OPEN, Instant.parse("2026-04-20T06:00:00Z"), null);
        insertTicket(tenantId, assetId, TicketState.OPEN, Instant.parse("2026-04-20T07:00:00Z"), null);
    }
}
