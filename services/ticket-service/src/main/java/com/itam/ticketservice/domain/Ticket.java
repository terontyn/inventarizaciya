package com.itam.ticketservice.domain;

import java.time.Instant;
import java.util.UUID;

public record Ticket(
    UUID id,
    UUID tenantId,
    UUID assetId,
    TicketState ticketState,
    Instant openedAt,
    Instant closedAt
) {
}
