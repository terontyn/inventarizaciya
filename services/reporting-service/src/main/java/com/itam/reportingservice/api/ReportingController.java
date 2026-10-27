package com.itam.reportingservice.api;

import com.itam.reportingservice.application.ReportingApplicationService;
import com.itam.reportingservice.application.ReportingAuthorizationService;
import com.itam.reportingservice.domain.AssetOperationalCurrentView;
import com.itam.reportingservice.domain.SoftwareRelationSummary;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reporting/assets")
public class ReportingController {

    private final ReportingApplicationService reportingApplicationService;
    private final ReportingAuthorizationService reportingAuthorizationService;
    private final AuthenticatedRequestContextExtractor requestContextExtractor;

    public ReportingController(
        ReportingApplicationService reportingApplicationService,
        ReportingAuthorizationService reportingAuthorizationService,
        AuthenticatedRequestContextExtractor requestContextExtractor
    ) {
        this.reportingApplicationService = reportingApplicationService;
        this.reportingAuthorizationService = reportingAuthorizationService;
        this.requestContextExtractor = requestContextExtractor;
    }

    @GetMapping
    public List<AssetOperationalCurrentViewResponse> listAssets(HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        reportingAuthorizationService.requireReportingReadAccess(tenantId, authenticatedUserId);
        return reportingApplicationService.findAllAssets(tenantId, authenticatedUserId).stream()
            .map(AssetOperationalCurrentViewResponse::from)
            .toList();
    }

    @GetMapping("/{assetId}")
    public AssetOperationalCurrentViewResponse getAsset(
        @PathVariable UUID assetId,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        reportingAuthorizationService.requireReportingReadAccess(tenantId, authenticatedUserId);
        return AssetOperationalCurrentViewResponse.from(
            reportingApplicationService.findAsset(tenantId, authenticatedUserId, assetId)
        );
    }

    public record AssetOperationalCurrentViewResponse(
        UUID assetId,
        UUID assignedUserId,
        UUID assignedLocationId,
        UUID warehouseId,
        UUID storagePlaceId,
        Boolean inStorageNow,
        String readinessState,
        Boolean hasOpenMaintenanceWork,
        Boolean hasOpenTickets,
        List<SoftwareRelationSummaryResponse> softwareRelations,
        boolean inventorySliceAvailable,
        boolean maintenanceSliceAvailable,
        boolean ticketSliceAvailable,
        boolean softwareSliceAvailable
    ) {

        private static AssetOperationalCurrentViewResponse from(AssetOperationalCurrentView view) {
            return new AssetOperationalCurrentViewResponse(
                view.assetId(),
                view.assignedUserId(),
                view.assignedLocationId(),
                view.warehouseId(),
                view.storagePlaceId(),
                view.inStorageNow(),
                view.readinessState(),
                view.hasOpenMaintenanceWork(),
                view.hasOpenTickets(),
                view.softwareRelations().stream()
                    .map(SoftwareRelationSummaryResponse::from)
                    .toList(),
                view.inventorySliceAvailable(),
                view.maintenanceSliceAvailable(),
                view.ticketSliceAvailable(),
                view.softwareSliceAvailable()
            );
        }
    }

    public record SoftwareRelationSummaryResponse(UUID softwareRecordId, String name) {

        private static SoftwareRelationSummaryResponse from(SoftwareRelationSummary summary) {
            return new SoftwareRelationSummaryResponse(summary.softwareRecordId(), summary.name());
        }
    }
}
