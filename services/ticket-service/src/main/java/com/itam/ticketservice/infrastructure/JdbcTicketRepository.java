package com.itam.ticketservice.infrastructure;

import com.itam.ticketservice.domain.Ticket;
import com.itam.ticketservice.domain.TicketRepository;
import com.itam.ticketservice.domain.TicketState;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTicketRepository implements TicketRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcTicketRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Ticket> findOpen(UUID tenantId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, asset_id, ticket_state, opened_at, closed_at "
                + "FROM tickets WHERE tenant_id = :tenantId AND ticket_state = 'OPEN' "
                + "ORDER BY opened_at DESC, id DESC",
            Map.of("tenantId", scopedTenantId),
            (resultSet, rowNum) -> mapTicket(resultSet)
        );
    }

    @Override
    public Optional<Ticket> findById(UUID tenantId, UUID ticketId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, asset_id, ticket_state, opened_at, closed_at "
                + "FROM tickets WHERE tenant_id = :tenantId AND id = :id",
            Map.of(
                "tenantId", scopedTenantId,
                "id", ticketId
            ),
            (resultSet, rowNum) -> mapTicket(resultSet)
        ).stream().findFirst();
    }

    @Override
    public List<Ticket> findOpenByAssetId(UUID tenantId, UUID assetId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, asset_id, ticket_state, opened_at, closed_at "
                + "FROM tickets WHERE tenant_id = :tenantId AND asset_id = :assetId AND ticket_state = 'OPEN' "
                + "ORDER BY opened_at DESC, id DESC",
            Map.of(
                "tenantId", scopedTenantId,
                "assetId", assetId
            ),
            (resultSet, rowNum) -> mapTicket(resultSet)
        );
    }

    @Override
    public Ticket createOpen(UUID tenantId, UUID assetId, Instant openedAt) {
        UUID ticketId = UUID.randomUUID();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", ticketId);
        parameters.put("tenantId", SqlTenantScope.requireTenantId(tenantId));
        parameters.put("assetId", assetId);
        parameters.put("ticketState", TicketState.OPEN.name());
        parameters.put("openedAt", Timestamp.from(openedAt));
        parameters.put("closedAt", null);
        jdbcTemplate.update(
            "INSERT INTO tickets (id, tenant_id, asset_id, ticket_state, opened_at, closed_at) "
                + "VALUES (:id, :tenantId, :assetId, :ticketState, :openedAt, :closedAt)",
            parameters
        );
        return new Ticket(ticketId, tenantId, assetId, TicketState.OPEN, openedAt, null);
    }

    @Override
    public Optional<Ticket> close(UUID tenantId, UUID ticketId, Instant closedAt) {
        int updated = jdbcTemplate.update(
            "UPDATE tickets SET ticket_state = :ticketState, closed_at = :closedAt "
                + "WHERE tenant_id = :tenantId AND id = :id AND ticket_state = 'OPEN'",
            Map.of(
                "tenantId", SqlTenantScope.requireTenantId(tenantId),
                "id", ticketId,
                "ticketState", TicketState.CLOSED.name(),
                "closedAt", Timestamp.from(closedAt)
            )
        );
        if (updated == 0) {
            return Optional.empty();
        }
        return findById(tenantId, ticketId);
    }

    private Ticket mapTicket(ResultSet resultSet) throws SQLException {
        Timestamp closedAt = resultSet.getTimestamp("closed_at");
        return new Ticket(
            resultSet.getObject("id", UUID.class),
            resultSet.getObject("tenant_id", UUID.class),
            resultSet.getObject("asset_id", UUID.class),
            TicketState.valueOf(resultSet.getString("ticket_state")),
            resultSet.getTimestamp("opened_at").toInstant(),
            closedAt == null ? null : closedAt.toInstant()
        );
    }
}
