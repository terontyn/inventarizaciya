package com.itam.reportingservice.infrastructure;

import static com.itam.reportingservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.reportingservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;

import com.itam.reportingservice.application.DerivedSliceResult;
import com.itam.reportingservice.application.SoftwareServiceSoftwareLookup;
import com.itam.reportingservice.domain.SoftwareRelationSummary;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Repository
public class HttpSoftwareServiceSoftwareLookup implements SoftwareServiceSoftwareLookup {

    private final RestTemplate restTemplate;
    private final String softwareServiceBaseUrl;

    public HttpSoftwareServiceSoftwareLookup(
        @Qualifier("softwareServiceRestTemplate") RestTemplate restTemplate,
        @Value("${integration.software-service.base-url}") String softwareServiceBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.softwareServiceBaseUrl = trimTrailingSlash(softwareServiceBaseUrl);
    }

    @Override
    public DerivedSliceResult<SoftwareSlice> findAssetSoftware(UUID tenantId, UUID authenticatedUserId, UUID assetId) {
        RequestEntity<Void> request = RequestEntity
            .get(URI.create(softwareServiceBaseUrl + "/api/v1/software/assets/" + assetId + "/software"))
            .header(AUTHENTICATED_TENANT_HEADER, tenantId.toString())
            .header(AUTHENTICATED_USER_HEADER, authenticatedUserId.toString())
            .build();
        try {
            ResponseEntity<SoftwareRecordSummaryResponse[]> response = restTemplate.exchange(
                request,
                SoftwareRecordSummaryResponse[].class
            );
            SoftwareRecordSummaryResponse[] body = response.getBody();
            List<SoftwareRelationSummary> relations = body == null
                ? List.of()
                : Arrays.stream(body)
                    .map(item -> new SoftwareRelationSummary(item.id(), item.name()))
                    .toList();
            return DerivedSliceResult.available(new SoftwareSlice(assetId, relations));
        } catch (RestClientException exception) {
            return DerivedSliceResult.unavailable();
        }
    }

    private String trimTrailingSlash(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private record SoftwareRecordSummaryResponse(UUID id, String name) {
    }
}
