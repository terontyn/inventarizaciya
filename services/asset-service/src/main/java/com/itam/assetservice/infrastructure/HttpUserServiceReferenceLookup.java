package com.itam.assetservice.infrastructure;

import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;

import com.itam.assetservice.application.ReferenceLookupUnavailableException;
import com.itam.assetservice.application.UserServiceReferenceLookup;
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
public class HttpUserServiceReferenceLookup implements UserServiceReferenceLookup {

    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    public HttpUserServiceReferenceLookup(
        RestTemplate userServiceRestTemplate,
        @Value("${integration.user-service.base-url}") String userServiceBaseUrl
    ) {
        this.restTemplate = userServiceRestTemplate;
        this.userServiceBaseUrl = trimTrailingSlash(userServiceBaseUrl);
    }

    @Override
    public boolean userExists(UUID tenantId, UUID userId) {
        return existsInTenantScope(tenantId, "/api/v1/users/" + userId);
    }

    @Override
    public boolean locationExists(UUID tenantId, UUID locationId) {
        return existsInTenantScope(tenantId, "/api/v1/locations/" + locationId);
    }

    private boolean existsInTenantScope(UUID tenantId, String path) {
        RequestEntity<Void> request = RequestEntity
            .get(URI.create(userServiceBaseUrl + path))
            .header(AUTHENTICATED_TENANT_HEADER, tenantId.toString())
            .build();
        try {
            ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound exception) {
            return false;
        } catch (RestClientException exception) {
            throw new ReferenceLookupUnavailableException("Unable to reach user-service", exception);
        }
    }

    private String trimTrailingSlash(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
