package com.itam.softwareservice.infrastructure;

import static com.itam.softwareservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.softwareservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;

import com.itam.softwareservice.application.ReferenceLookupUnavailableException;
import com.itam.softwareservice.application.UserServiceUserLookup;
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
public class HttpUserServiceUserLookup implements UserServiceUserLookup {

    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    public HttpUserServiceUserLookup(
        RestTemplate userServiceRestTemplate,
        @Value("${integration.user-service.base-url}") String userServiceBaseUrl
    ) {
        this.restTemplate = userServiceRestTemplate;
        this.userServiceBaseUrl = trimTrailingSlash(userServiceBaseUrl);
    }

    @Override
    public boolean userExists(UUID tenantId, UUID authenticatedUserId, UUID userId) {
        RequestEntity<Void> request = RequestEntity
            .get(URI.create(userServiceBaseUrl + "/api/v1/users/" + userId))
            .header(AUTHENTICATED_TENANT_HEADER, tenantId.toString())
            .header(AUTHENTICATED_USER_HEADER, authenticatedUserId.toString())
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
