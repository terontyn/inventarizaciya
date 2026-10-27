package com.itam.reportingservice.infrastructure;

import static com.itam.reportingservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.reportingservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;

import com.itam.reportingservice.application.DerivedSliceResult;
import com.itam.reportingservice.application.MaintenanceServiceMaintenanceLookup;
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
public class HttpMaintenanceServiceMaintenanceLookup implements MaintenanceServiceMaintenanceLookup {

    private final RestTemplate restTemplate;
    private final String maintenanceServiceBaseUrl;

    public HttpMaintenanceServiceMaintenanceLookup(
        @Qualifier("maintenanceServiceRestTemplate") RestTemplate restTemplate,
        @Value("${integration.maintenance-service.base-url}") String maintenanceServiceBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.maintenanceServiceBaseUrl = trimTrailingSlash(maintenanceServiceBaseUrl);
    }

    @Override
    public DerivedSliceResult<MaintenanceSlice> findAssetMaintenance(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID assetId
    ) {
        RequestEntity<Void> request = RequestEntity
            .get(URI.create(maintenanceServiceBaseUrl + "/api/v1/maintenance/assets/" + assetId))
            .header(AUTHENTICATED_TENANT_HEADER, tenantId.toString())
            .header(AUTHENTICATED_USER_HEADER, authenticatedUserId.toString())
            .build();
        try {
            ResponseEntity<AssetMaintenanceResponse> response = restTemplate.exchange(request, AssetMaintenanceResponse.class);
            AssetMaintenanceResponse body = response.getBody();
            if (body == null) {
                return DerivedSliceResult.unavailable();
            }
            return DerivedSliceResult.available(new MaintenanceSlice(body.assetId(), body.hasOpenWork()));
        } catch (RestClientException exception) {
            return DerivedSliceResult.unavailable();
        }
    }

    private String trimTrailingSlash(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private record AssetMaintenanceResponse(
        UUID assetId,
        Boolean hasOpenWork,
        Boolean serviceableNow,
        UUID workId,
        String workType,
        String workState,
        String openedAt,
        String completedAt
    ) {
    }
}
