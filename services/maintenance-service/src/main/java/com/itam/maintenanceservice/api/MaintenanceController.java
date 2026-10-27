package com.itam.maintenanceservice.api;

import com.itam.maintenanceservice.application.MaintenanceApplicationService;
import com.itam.maintenanceservice.application.MaintenanceAuthorizationService;
import com.itam.maintenanceservice.domain.MaintenanceWork;
import com.itam.maintenanceservice.domain.MaintenanceWorkState;
import com.itam.maintenanceservice.domain.MaintenanceWorkType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class MaintenanceController {

    private final MaintenanceApplicationService maintenanceApplicationService;
    private final MaintenanceAuthorizationService maintenanceAuthorizationService;
    private final AuthenticatedRequestContextExtractor requestContextExtractor;

    public MaintenanceController(
        MaintenanceApplicationService maintenanceApplicationService,
        MaintenanceAuthorizationService maintenanceAuthorizationService,
        AuthenticatedRequestContextExtractor requestContextExtractor
    ) {
        this.maintenanceApplicationService = maintenanceApplicationService;
        this.maintenanceAuthorizationService = maintenanceAuthorizationService;
        this.requestContextExtractor = requestContextExtractor;
    }

    @GetMapping("/api/v1/maintenance/work/open")
    public List<MaintenanceWorkResponse> listOpenWork(HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        maintenanceAuthorizationService.requireMaintenanceReadAccess(tenantId, authenticatedUserId);
        return maintenanceApplicationService.findOpenWork(tenantId).stream()
            .map(MaintenanceWorkResponse::from)
            .toList();
    }

    @GetMapping("/api/v1/maintenance/work/{id}")
    public MaintenanceWorkResponse getWork(@PathVariable UUID id, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        maintenanceAuthorizationService.requireMaintenanceReadAccess(tenantId, authenticatedUserId);
        return MaintenanceWorkResponse.from(maintenanceApplicationService.findWork(tenantId, id));
    }

    @GetMapping("/api/v1/maintenance/assets/{assetId}")
    public AssetMaintenanceResponse getAssetMaintenanceView(@PathVariable UUID assetId, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        maintenanceAuthorizationService.requireMaintenanceReadAccess(tenantId, authenticatedUserId);
        return AssetMaintenanceResponse.from(
            maintenanceApplicationService.findAssetView(tenantId, authenticatedUserId, assetId)
        );
    }

    @PostMapping("/api/v1/maintenance/assets/{assetId}/work")
    public ResponseEntity<MaintenanceWorkResponse> openWork(
        @PathVariable UUID assetId,
        @Valid @RequestBody OpenMaintenanceWorkRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        maintenanceAuthorizationService.requireMaintenanceMutateAccess(tenantId, authenticatedUserId);
        MaintenanceWork work = maintenanceApplicationService.openWork(
            tenantId,
            authenticatedUserId,
            assetId,
            request.workType(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return ResponseEntity
            .created(URI.create("/api/v1/maintenance/work/" + work.id()))
            .body(MaintenanceWorkResponse.from(work));
    }

    @PostMapping("/api/v1/maintenance/work/{id}/complete")
    public MaintenanceWorkResponse completeWork(@PathVariable UUID id, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        maintenanceAuthorizationService.requireMaintenanceMutateAccess(tenantId, authenticatedUserId);
        return MaintenanceWorkResponse.from(
            maintenanceApplicationService.completeWork(
                tenantId,
                authenticatedUserId,
                id,
                requestContextExtractor.requireAuditMetadata(servletRequest)
            )
        );
    }

    public record OpenMaintenanceWorkRequest(@NotNull MaintenanceWorkType workType) {
    }

    public record MaintenanceWorkResponse(
        UUID id,
        UUID assetId,
        MaintenanceWorkType workType,
        MaintenanceWorkState workState,
        Instant openedAt,
        Instant completedAt
    ) {

        private static MaintenanceWorkResponse from(MaintenanceWork work) {
            return new MaintenanceWorkResponse(
                work.id(),
                work.assetId(),
                work.workType(),
                work.workState(),
                work.openedAt(),
                work.completedAt()
            );
        }
    }

    public record AssetMaintenanceResponse(
        UUID assetId,
        boolean hasOpenWork,
        boolean serviceableNow,
        UUID workId,
        MaintenanceWorkType workType,
        MaintenanceWorkState workState,
        Instant openedAt,
        Instant completedAt
    ) {

        private static AssetMaintenanceResponse from(MaintenanceApplicationService.AssetMaintenanceView view) {
            return new AssetMaintenanceResponse(
                view.assetId(),
                view.hasOpenWork(),
                view.serviceableNow(),
                view.workId(),
                view.workType(),
                view.workState(),
                view.openedAt(),
                view.completedAt()
            );
        }
    }
}
