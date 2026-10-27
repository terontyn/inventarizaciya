package com.itam.reportingservice.infrastructure;

import static com.itam.reportingservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.reportingservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;

import com.itam.reportingservice.application.AssetServiceAssetLookup;
import com.itam.reportingservice.application.ReferenceLookupUnavailableException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Repository
public class HttpAssetServiceAssetLookup implements AssetServiceAssetLookup {

    private final RestTemplate restTemplate;
    private final String assetServiceBaseUrl;

    public HttpAssetServiceAssetLookup(
        @Qualifier("assetServiceRestTemplate") RestTemplate restTemplate,
        @Value("${integration.asset-service.base-url}") String assetServiceBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.assetServiceBaseUrl = trimTrailingSlash(assetServiceBaseUrl);
    }

    @Override
    public List<AssetSummary> findAllAssets(UUID tenantId, UUID authenticatedUserId) {
        RequestEntity<Void> request = baseRequest("/api/v1/assets", tenantId, authenticatedUserId);
        try {
            ResponseEntity<AssetResponse[]> response = restTemplate.exchange(request, AssetResponse[].class);
            AssetResponse[] body = response.getBody();
            if (body == null) {
                return List.of();
            }
            return Arrays.stream(body)
                .map(HttpAssetServiceAssetLookup::toSummary)
                .toList();
        } catch (RestClientException exception) {
            throw new ReferenceLookupUnavailableException("Unable to read assets via asset-service", exception);
        }
    }

    @Override
    public Optional<AssetSummary> findAsset(UUID tenantId, UUID authenticatedUserId, UUID assetId) {
        RequestEntity<Void> request = baseRequest("/api/v1/assets/" + assetId, tenantId, authenticatedUserId);
        try {
            ResponseEntity<AssetResponse> response = restTemplate.exchange(request, AssetResponse.class);
            AssetResponse body = response.getBody();
            return body == null ? Optional.empty() : Optional.of(toSummary(body));
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        } catch (RestClientException exception) {
            throw new ReferenceLookupUnavailableException("Unable to read asset via asset-service", exception);
        }
    }

    private RequestEntity<Void> baseRequest(String path, UUID tenantId, UUID authenticatedUserId) {
        return RequestEntity
            .get(URI.create(assetServiceBaseUrl + path))
            .header(AUTHENTICATED_TENANT_HEADER, tenantId.toString())
            .header(AUTHENTICATED_USER_HEADER, authenticatedUserId.toString())
            .build();
    }

    private static AssetSummary toSummary(AssetResponse response) {
        return new AssetSummary(response.id(), response.assignedUserId(), response.locationId());
    }

    private String trimTrailingSlash(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private record AssetResponse(
        UUID id,
        String assetTag,
        String serialNumber,
        String assetType,
        String status,
        UUID assignedUserId,
        UUID locationId
    ) {
    }
}
