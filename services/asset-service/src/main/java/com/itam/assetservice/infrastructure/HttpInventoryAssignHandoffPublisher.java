package com.itam.assetservice.infrastructure;

import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_ACTOR_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.CORRELATION_ID_HEADER;

import com.itam.assetservice.application.AuditMetadata;
import com.itam.assetservice.application.InventoryAssignHandoffPublisher;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Repository
@ConditionalOnProperty(
    prefix = "integration.inventory-service.assign-handoff",
    name = "enabled",
    havingValue = "true"
)
public class HttpInventoryAssignHandoffPublisher implements InventoryAssignHandoffPublisher {

    private final RestTemplate restTemplate;
    private final Executor executor;
    private final String inventoryServiceBaseUrl;

    public HttpInventoryAssignHandoffPublisher(
        RestTemplate inventoryServiceRestTemplate,
        @Qualifier("inventoryAssignHandoffExecutor") Executor executor,
        @Value("${integration.inventory-service.base-url}") String inventoryServiceBaseUrl
    ) {
        this.restTemplate = inventoryServiceRestTemplate;
        this.executor = executor;
        this.inventoryServiceBaseUrl = trimTrailingSlash(inventoryServiceBaseUrl);
    }

    @Override
    public void publishAfterAssign(UUID tenantId, UUID authenticatedUserId, UUID assetId, AuditMetadata auditMetadata) {
        Runnable dispatch = () -> {
            RequestEntity<Void> request = RequestEntity
                .post(URI.create(inventoryServiceBaseUrl + "/internal/api/v1/inventory/assets/" + assetId + "/assign-handoff"))
                .header(AUTHENTICATED_TENANT_HEADER, tenantId.toString())
                .header(AUTHENTICATED_USER_HEADER, authenticatedUserId.toString())
                .header(AUTHENTICATED_ACTOR_HEADER, auditMetadata.actor())
                .header(CORRELATION_ID_HEADER, auditMetadata.correlationId())
                .build();
            try {
                restTemplate.exchange(request, Void.class);
            } catch (RestClientException exception) {
                // First contour accepts eventual consistency and does not roll back assign.
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    executor.execute(dispatch);
                }
            });
            return;
        }

        executor.execute(dispatch);
    }

    private String trimTrailingSlash(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
