package com.itam.ticketservice.api;

import com.itam.ticketservice.application.TicketApplicationService;
import com.itam.ticketservice.application.TicketAuthorizationService;
import com.itam.ticketservice.domain.Ticket;
import com.itam.ticketservice.domain.TicketState;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TicketController {

    private final TicketApplicationService ticketApplicationService;
    private final TicketAuthorizationService ticketAuthorizationService;
    private final AuthenticatedRequestContextExtractor requestContextExtractor;

    public TicketController(
        TicketApplicationService ticketApplicationService,
        TicketAuthorizationService ticketAuthorizationService,
        AuthenticatedRequestContextExtractor requestContextExtractor
    ) {
        this.ticketApplicationService = ticketApplicationService;
        this.ticketAuthorizationService = ticketAuthorizationService;
        this.requestContextExtractor = requestContextExtractor;
    }

    @GetMapping("/api/v1/tickets/open")
    public List<TicketResponse> listOpenTickets(HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        ticketAuthorizationService.requireTicketReadAccess(tenantId, authenticatedUserId);
        return ticketApplicationService.findOpenTickets(tenantId).stream()
            .map(TicketResponse::from)
            .toList();
    }

    @GetMapping("/api/v1/tickets/{id}")
    public TicketResponse getTicket(@PathVariable UUID id, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        ticketAuthorizationService.requireTicketReadAccess(tenantId, authenticatedUserId);
        return TicketResponse.from(ticketApplicationService.findTicket(tenantId, id));
    }

    @GetMapping("/api/v1/tickets/assets/{assetId}")
    public List<TicketResponse> getOpenTicketsForAsset(@PathVariable UUID assetId, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        ticketAuthorizationService.requireTicketReadAccess(tenantId, authenticatedUserId);
        return ticketApplicationService.findOpenTicketsForAsset(tenantId, authenticatedUserId, assetId).stream()
            .map(TicketResponse::from)
            .toList();
    }

    @PostMapping("/api/v1/tickets/assets/{assetId}")
    public ResponseEntity<TicketResponse> openTicket(@PathVariable UUID assetId, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        ticketAuthorizationService.requireTicketMutateAccess(tenantId, authenticatedUserId);
        Ticket ticket = ticketApplicationService.openTicket(
            tenantId,
            authenticatedUserId,
            assetId,
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return ResponseEntity
            .created(URI.create("/api/v1/tickets/" + ticket.id()))
            .body(TicketResponse.from(ticket));
    }

    @PostMapping("/api/v1/tickets/{id}/close")
    public TicketResponse closeTicket(@PathVariable UUID id, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        ticketAuthorizationService.requireTicketMutateAccess(tenantId, authenticatedUserId);
        return TicketResponse.from(ticketApplicationService.closeTicket(
            tenantId,
            authenticatedUserId,
            id,
            requestContextExtractor.requireAuditMetadata(servletRequest)
        ));
    }

    public record TicketResponse(
        UUID id,
        UUID assetId,
        TicketState ticketState,
        Instant openedAt,
        Instant closedAt
    ) {

        private static TicketResponse from(Ticket ticket) {
            return new TicketResponse(
                ticket.id(),
                ticket.assetId(),
                ticket.ticketState(),
                ticket.openedAt(),
                ticket.closedAt()
            );
        }
    }
}
