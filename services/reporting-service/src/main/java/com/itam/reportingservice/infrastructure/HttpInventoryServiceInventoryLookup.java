package com.itam.reportingservice.infrastructure;

import static com.itam.reportingservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.reportingservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;

import com.itam.reportingservice.application.DerivedSliceResult;
import com.itam.reportingservice.application.InventoryServiceInventoryLookup;
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
public class HttpInventoryServiceInventoryLookup implements InventoryServiceInventoryLookup {

    private final RestTemplate restTemplate;
    private final String inventoryServiceBaseUrl;

    public HttpInventoryServiceInventoryLookup(
        @Qualifier("inventoryServiceRestTemplate") RestTemplate restTemplate,
        @Value("${integration.inventory-service.base-url}") String inventoryServiceBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.inventoryServiceBaseUrl = trimTrailingSlash(inventoryServiceBaseUrl);
    }

    @Override
    public DerivedSliceResult<InventorySlice> findAssetInventory(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID assetId
    ) {
        RequestEntity<Void> request = RequestEntity
            .get(URI.create(inventoryServiceBaseUrl + "/api/v1/inventory/assets/" + assetId))
            .header(AUTHENTICATED_TENANT_HEADER, tenantId.toString())
            .header(AUTHENTICATED_USER_HEADER, authenticatedUserId.toString())
            .build();
        try {
            ResponseEntity<InventoryAssetResponse> response = restTemplate.exchange(request, InventoryAssetResponse.class);
            InventoryAssetResponse body = response.getBody();
            if (body == null) {
                return DerivedSliceResult.unavailable();
            }
            return DerivedSliceResult.available(new InventorySlice(
                body.inStorageNow(),
                body.warehouseId(),
                body.storagePlaceId(),
                body.readinessState()
            ));
        } catch (RestClientException exception) {
            return DerivedSliceResult.unavailable();
        }
    }

    private String trimTrailingSlash(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private record InventoryAssetResponse(
        UUID assetId,
        Boolean inStorageNow,
        UUID warehouseId,
        UUID storagePlaceId,
        String readinessState
    ) {
    }
}
