package com.itam.softwareservice.infrastructure;

import static com.itam.softwareservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.softwareservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;

import com.itam.softwareservice.application.AssetServiceAssetLookup;
import com.itam.softwareservice.application.ReferenceLookupUnavailableException;
import java.net.URI;
import java.util.UUID;
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
        RestTemplate assetServiceRestTemplate,
        @Value("${integration.asset-service.base-url}") String assetServiceBaseUrl
    ) {
        this.restTemplate = assetServiceRestTemplate;
        this.assetServiceBaseUrl = trimTrailingSlash(assetServiceBaseUrl);
    }

    @Override
    public boolean assetExists(UUID tenantId, UUID authenticatedUserId, UUID assetId) {
        RequestEntity<Void> request = RequestEntity
            .get(URI.create(assetServiceBaseUrl + "/api/v1/assets/" + assetId))
            .header(AUTHENTICATED_TENANT_HEADER, tenantId.toString())
            .header(AUTHENTICATED_USER_HEADER, authenticatedUserId.toString())
            .build();
        try {
            ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound exception) {
            return false;
        } catch (RestClientException exception) {
            throw new ReferenceLookupUnavailableException("Unable to reach asset-service", exception);
        }
    }

    private String trimTrailingSlash(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
