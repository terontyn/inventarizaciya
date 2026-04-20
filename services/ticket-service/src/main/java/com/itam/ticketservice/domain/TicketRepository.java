package com.itam.ticketservice.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository {

    List<Ticket> findOpen(UUID tenantId);

    Optional<Ticket> findById(UUID tenantId, UUID ticketId);

    List<Ticket> findOpenByAssetId(UUID tenantId, UUID assetId);

    Ticket createOpen(UUID tenantId, UUID assetId, Instant openedAt);

    Optional<Ticket> close(UUID tenantId, UUID ticketId, Instant closedAt);
}
