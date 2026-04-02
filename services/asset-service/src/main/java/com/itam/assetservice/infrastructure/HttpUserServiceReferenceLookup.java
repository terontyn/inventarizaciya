package com.itam.assetservice.infrastructure;

import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;

import com.itam.assetservice.application.ReferenceLookupUnavailableException;
import com.itam.assetservice.application.UserServiceRoleLookup;
import com.itam.assetservice.application.UserServiceReferenceLookup;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Repository
public class HttpUserServiceReferenceLookup implements UserServiceReferenceLookup, UserServiceRoleLookup {

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
    public boolean userExists(UUID tenantId, UUID authenticatedUserId, UUID userId) {
        return existsInTenantScope(tenantId, authenticatedUserId, "/api/v1/users/" + userId);
    }

    @Override
    public boolean locationExists(UUID tenantId, UUID authenticatedUserId, UUID locationId) {
        return existsInTenantScope(tenantId, authenticatedUserId, "/api/v1/locations/" + locationId);
    }

    @Override
    public List<String> findRoleNames(UUID tenantId, UUID userId) {
        RequestEntity<Void> request = RequestEntity
            .get(URI.create(userServiceBaseUrl + "/internal/api/v1/users/" + userId + "/roles"))
            .header(AUTHENTICATED_TENANT_HEADER, tenantId.toString())
            .build();
        try {
            ResponseEntity<RoleResolutionResponse> response = restTemplate.exchange(
                request,
                RoleResolutionResponse.class
            );
            RoleResolutionResponse body = response.getBody();
            return body == null || body.roleNames() == null ? List.of() : body.roleNames();
        } catch (RestClientException exception) {
            throw new ReferenceLookupUnavailableException("Unable to reach user-service", exception);
        }
    }

    private boolean existsInTenantScope(UUID tenantId, UUID authenticatedUserId, String path) {
        RequestEntity<Void> request = RequestEntity
            .get(URI.create(userServiceBaseUrl + path))
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

    private record RoleResolutionResponse(UUID userId, List<String> roleNames) {
    }
}
