package com.itam.softwareservice.api;

import com.itam.softwareservice.application.SoftwareApplicationService;
import com.itam.softwareservice.application.SoftwareAuthorizationService;
import com.itam.softwareservice.domain.SoftwareRecord;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
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
public class SoftwareController {

    private final SoftwareApplicationService softwareApplicationService;
    private final SoftwareAuthorizationService softwareAuthorizationService;
    private final AuthenticatedRequestContextExtractor requestContextExtractor;

    public SoftwareController(
        SoftwareApplicationService softwareApplicationService,
        SoftwareAuthorizationService softwareAuthorizationService,
        AuthenticatedRequestContextExtractor requestContextExtractor
    ) {
        this.softwareApplicationService = softwareApplicationService;
        this.softwareAuthorizationService = softwareAuthorizationService;
        this.requestContextExtractor = requestContextExtractor;
    }

    @GetMapping("/api/v1/software/records")
    public List<SoftwareRecordSummaryResponse> listRecords(HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        softwareAuthorizationService.requireSoftwareReadAccess(tenantId, authenticatedUserId);
        return softwareApplicationService.findAllRecords(tenantId).stream()
            .map(SoftwareRecordSummaryResponse::from)
            .toList();
    }

    @GetMapping("/api/v1/software/records/{id}")
    public SoftwareRecordDetailResponse getRecord(@PathVariable UUID id, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        softwareAuthorizationService.requireSoftwareReadAccess(tenantId, authenticatedUserId);
        return SoftwareRecordDetailResponse.from(softwareApplicationService.findRecord(tenantId, id));
    }

    @GetMapping("/api/v1/software/users/{userId}/software")
    public List<SoftwareRecordSummaryResponse> getUserSoftware(@PathVariable UUID userId, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        softwareAuthorizationService.requireSoftwareReadAccess(tenantId, authenticatedUserId);
        return softwareApplicationService.findSoftwareForUser(tenantId, authenticatedUserId, userId).stream()
            .map(SoftwareRecordSummaryResponse::from)
            .toList();
    }

    @GetMapping("/api/v1/software/assets/{assetId}/software")
    public List<SoftwareRecordSummaryResponse> getAssetSoftware(@PathVariable UUID assetId, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        softwareAuthorizationService.requireSoftwareReadAccess(tenantId, authenticatedUserId);
        return softwareApplicationService.findSoftwareForAsset(tenantId, authenticatedUserId, assetId).stream()
            .map(SoftwareRecordSummaryResponse::from)
            .toList();
    }

    @PostMapping("/api/v1/software/records")
    public ResponseEntity<SoftwareRecordDetailResponse> createRecord(
        @Valid @RequestBody CreateSoftwareRecordRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        softwareAuthorizationService.requireSoftwareMutateAccess(tenantId, authenticatedUserId);
        SoftwareApplicationService.SoftwareRecordView view = softwareApplicationService.createRecord(
            tenantId,
            request.name(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return ResponseEntity
            .created(URI.create("/api/v1/software/records/" + view.id()))
            .body(SoftwareRecordDetailResponse.from(view));
    }

    @PostMapping("/api/v1/software/records/{id}/allocate-to-user")
    public SoftwareRecordDetailResponse allocateToUser(
        @PathVariable UUID id,
        @Valid @RequestBody UserAllocationRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        softwareAuthorizationService.requireSoftwareMutateAccess(tenantId, authenticatedUserId);
        return SoftwareRecordDetailResponse.from(softwareApplicationService.allocateToUser(
            tenantId,
            authenticatedUserId,
            id,
            request.userId(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        ));
    }

    @PostMapping("/api/v1/software/records/{id}/allocate-to-asset")
    public SoftwareRecordDetailResponse allocateToAsset(
        @PathVariable UUID id,
        @Valid @RequestBody AssetAllocationRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        softwareAuthorizationService.requireSoftwareMutateAccess(tenantId, authenticatedUserId);
        return SoftwareRecordDetailResponse.from(softwareApplicationService.allocateToAsset(
            tenantId,
            authenticatedUserId,
            id,
            request.assetId(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        ));
    }

    @PostMapping("/api/v1/software/records/{id}/remove-from-user")
    public SoftwareRecordDetailResponse removeFromUser(
        @PathVariable UUID id,
        @Valid @RequestBody UserAllocationRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        softwareAuthorizationService.requireSoftwareMutateAccess(tenantId, authenticatedUserId);
        return SoftwareRecordDetailResponse.from(softwareApplicationService.removeFromUser(
            tenantId,
            authenticatedUserId,
            id,
            request.userId(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        ));
    }

    @PostMapping("/api/v1/software/records/{id}/remove-from-asset")
    public SoftwareRecordDetailResponse removeFromAsset(
        @PathVariable UUID id,
        @Valid @RequestBody AssetAllocationRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        softwareAuthorizationService.requireSoftwareMutateAccess(tenantId, authenticatedUserId);
        return SoftwareRecordDetailResponse.from(softwareApplicationService.removeFromAsset(
            tenantId,
            authenticatedUserId,
            id,
            request.assetId(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        ));
    }

    public record CreateSoftwareRecordRequest(@NotBlank String name) {
    }

    public record UserAllocationRequest(@NotNull UUID userId) {
    }

    public record AssetAllocationRequest(@NotNull UUID assetId) {
    }

    public record SoftwareRecordSummaryResponse(UUID id, String name) {

        private static SoftwareRecordSummaryResponse from(SoftwareRecord record) {
            return new SoftwareRecordSummaryResponse(record.id(), record.name());
        }
    }

    public record SoftwareRecordDetailResponse(
        UUID id,
        String name,
        List<UUID> allocatedUserIds,
        List<UUID> allocatedAssetIds
    ) {

        private static SoftwareRecordDetailResponse from(SoftwareApplicationService.SoftwareRecordView view) {
            return new SoftwareRecordDetailResponse(
                view.id(),
                view.name(),
                view.allocatedUserIds(),
                view.allocatedAssetIds()
            );
        }
    }
}
