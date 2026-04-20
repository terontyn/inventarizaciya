package com.itam.ticketservice.application;

import com.itam.ticketservice.domain.AuditAction;
import com.itam.ticketservice.domain.AuditEntity;
import com.itam.ticketservice.domain.Ticket;
import com.itam.ticketservice.domain.TicketRepository;
import com.itam.ticketservice.domain.TicketState;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TicketApplicationService {

    private final TicketRepository ticketRepository;
    private final AssetServiceAssetLookup assetServiceAssetLookup;
    private final AuditWriter auditWriter;
    private final Clock clock;

    public TicketApplicationService(
        TicketRepository ticketRepository,
        AssetServiceAssetLookup assetServiceAssetLookup,
        AuditWriter auditWriter,
        Clock clock
    ) {
        this.ticketRepository = ticketRepository;
        this.assetServiceAssetLookup = assetServiceAssetLookup;
        this.auditWriter = auditWriter;
        this.clock = clock;
    }

    public List<Ticket> findOpenTickets(UUID tenantId) {
        return ticketRepository.findOpen(tenantId);
    }

    public Ticket findTicket(UUID tenantId, UUID ticketId) {
        return ticketRepository.findById(tenantId, ticketId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
    }

    public List<Ticket> findOpenTicketsForAsset(UUID tenantId, UUID authenticatedUserId, UUID assetId) {
        requireTrackedAsset(tenantId, authenticatedUserId, assetId);
        return ticketRepository.findOpenByAssetId(tenantId, assetId);
    }

    @Transactional
    public Ticket openTicket(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID assetId,
        AuditMetadata auditMetadata
    ) {
        requireTrackedAsset(tenantId, authenticatedUserId, assetId);
        Ticket ticket = ticketRepository.createOpen(tenantId, assetId, Instant.now(clock));
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.TICKET,
            ticket.id(),
            AuditAction.OPEN_TICKET,
            new TicketAuditPayload(ticket.assetId(), ticket.ticketState(), ticket.openedAt(), ticket.closedAt())
        );
        return ticket;
    }

    @Transactional
    public Ticket closeTicket(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID ticketId,
        AuditMetadata auditMetadata
    ) {
        Ticket existingTicket = ticketRepository.findById(tenantId, ticketId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        requireTrackedAsset(tenantId, authenticatedUserId, existingTicket.assetId());
        if (existingTicket.ticketState() != TicketState.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ticket is not OPEN");
        }

        Ticket closedTicket = ticketRepository.close(tenantId, ticketId, Instant.now(clock))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Ticket is not OPEN"));
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.TICKET,
            closedTicket.id(),
            AuditAction.CLOSE_TICKET,
            new TicketCloseAuditPayload(
                closedTicket.assetId(),
                existingTicket.ticketState(),
                closedTicket.ticketState(),
                closedTicket.closedAt()
            )
        );
        return closedTicket;
    }

    private void requireTrackedAsset(UUID tenantId, UUID authenticatedUserId, UUID assetId) {
        try {
            boolean exists = assetServiceAssetLookup.assetExists(tenantId, authenticatedUserId, assetId);
            if (!exists) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found");
            }
        } catch (ReferenceLookupUnavailableException exception) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Unable to validate asset via asset-service",
                exception
            );
        }
    }

    private record TicketAuditPayload(
        UUID assetId,
        TicketState ticketState,
        Instant openedAt,
        Instant closedAt
    ) {
    }

    private record TicketCloseAuditPayload(
        UUID assetId,
        TicketState previousTicketState,
        TicketState newTicketState,
        Instant closedAt
    ) {
    }
}
