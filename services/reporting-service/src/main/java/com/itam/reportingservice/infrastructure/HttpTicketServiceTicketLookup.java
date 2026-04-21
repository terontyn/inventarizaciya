package com.itam.reportingservice.infrastructure;

import static com.itam.reportingservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.reportingservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;

import com.itam.reportingservice.application.DerivedSliceResult;
import com.itam.reportingservice.application.TicketServiceTicketLookup;
import java.net.URI;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Repository
public class HttpTicketServiceTicketLookup implements TicketServiceTicketLookup {

    private final RestTemplate restTemplate;
    private final String ticketServiceBaseUrl;

    public HttpTicketServiceTicketLookup(
        @Qualifier("ticketServiceRestTemplate") RestTemplate restTemplate,
        @Value("${integration.ticket-service.base-url}") String ticketServiceBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.ticketServiceBaseUrl = trimTrailingSlash(ticketServiceBaseUrl);
    }

    @Override
    public DerivedSliceResult<TicketSlice> findAssetTickets(UUID tenantId, UUID authenticatedUserId, UUID assetId) {
        RequestEntity<Void> request = RequestEntity
            .get(URI.create(ticketServiceBaseUrl + "/api/v1/tickets/assets/" + assetId))
            .header(AUTHENTICATED_TENANT_HEADER, tenantId.toString())
            .header(AUTHENTICATED_USER_HEADER, authenticatedUserId.toString())
            .build();
        try {
            ResponseEntity<TicketResponse[]> response = restTemplate.exchange(request, TicketResponse[].class);
            TicketResponse[] body = response.getBody();
            return DerivedSliceResult.available(new TicketSlice(assetId, body != null && body.length > 0));
        } catch (RestClientException exception) {
            return DerivedSliceResult.unavailable();
        }
    }

    private String trimTrailingSlash(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private record TicketResponse(
        UUID id,
        UUID assetId,
        String ticketState,
        String openedAt,
        String closedAt
    ) {
    }
}
