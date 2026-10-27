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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Repository
@ConditionalOnProperty(
    prefix = "integration.inventory-service.assign-handoff",
    name = "enabled",
    havingValue = "true"
)
public class HttpInventoryAssignHandoffPublisher implements InventoryAssignHandoffPublisher {

    private static final Logger log = LoggerFactory.getLogger(HttpInventoryAssignHandoffPublisher.class);
    private static final int MAX_DISPATCH_ATTEMPTS = 3;
    private static final long RETRY_BACKOFF_MILLIS = 500;

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
            for (int attempt = 1; attempt <= MAX_DISPATCH_ATTEMPTS; attempt++) {
                try {
                    restTemplate.exchange(request, Void.class);
                    return;
                } catch (RestClientException exception) {
                    // First contour accepts eventual consistency and does not roll back assign,
                    // but failures must be visible and retried instead of silently dropped.
                    if (attempt == MAX_DISPATCH_ATTEMPTS) {
                        log.error(
                            "Assign handoff for asset {} (tenant {}, correlation {}) failed after {} attempts; event lost",
                            assetId, tenantId, auditMetadata.correlationId(), MAX_DISPATCH_ATTEMPTS, exception
                        );
                        return;
                    }
                    log.warn(
                        "Assign handoff for asset {} (tenant {}, correlation {}) failed on attempt {}/{}; retrying",
                        assetId, tenantId, auditMetadata.correlationId(), attempt, MAX_DISPATCH_ATTEMPTS
                    );
                    try {
                        Thread.sleep(RETRY_BACKOFF_MILLIS * attempt);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
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
