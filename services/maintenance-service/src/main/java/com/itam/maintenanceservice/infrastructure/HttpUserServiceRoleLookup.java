package com.itam.maintenanceservice.infrastructure;

import static com.itam.maintenanceservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;

import com.itam.maintenanceservice.application.ReferenceLookupUnavailableException;
import com.itam.maintenanceservice.application.UserServiceRoleLookup;
import java.net.URI;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Repository
public class HttpUserServiceRoleLookup implements UserServiceRoleLookup {

    private static final ParameterizedTypeReference<RoleResolutionResponse> ROLE_RESPONSE_TYPE =
        new ParameterizedTypeReference<>() {
        };

    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    public HttpUserServiceRoleLookup(
        RestTemplate userServiceRestTemplate,
        @Value("${integration.user-service.base-url}") String userServiceBaseUrl
    ) {
        this.restTemplate = userServiceRestTemplate;
        this.userServiceBaseUrl = trimTrailingSlash(userServiceBaseUrl);
    }

    @Override
    public Set<String> findRoleNames(UUID tenantId, UUID authenticatedUserId) {
        RequestEntity<Void> request = RequestEntity
            .get(URI.create(userServiceBaseUrl + "/internal/api/v1/users/" + authenticatedUserId + "/roles"))
            .header(AUTHENTICATED_TENANT_HEADER, tenantId.toString())
            .build();
        try {
            ResponseEntity<RoleResolutionResponse> response = restTemplate.exchange(request, ROLE_RESPONSE_TYPE);
            RoleResolutionResponse body = response.getBody();
            return body == null || body.roleNames() == null ? Set.of() : Set.copyOf(body.roleNames());
        } catch (RestClientException exception) {
            throw new ReferenceLookupUnavailableException("Unable to reach user-service", exception);
        }
    }

    private String trimTrailingSlash(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private record RoleResolutionResponse(UUID userId, java.util.List<String> roleNames) {
    }
}
