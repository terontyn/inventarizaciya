package com.itam.assetservice.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.itam.assetservice.application.AssetApplicationService;
import com.itam.assetservice.application.AssetResponsibilityApplicationService;
import com.itam.assetservice.domain.Asset;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/assets")
public class AssetController {

    private final AssetApplicationService assetApplicationService;
    private final AssetResponsibilityApplicationService assetResponsibilityApplicationService;
    private final AuthenticatedRequestContextExtractor requestContextExtractor;

    public AssetController(
        AssetApplicationService assetApplicationService,
        AssetResponsibilityApplicationService assetResponsibilityApplicationService,
        AuthenticatedRequestContextExtractor requestContextExtractor
    ) {
        this.assetApplicationService = assetApplicationService;
        this.assetResponsibilityApplicationService = assetResponsibilityApplicationService;
        this.requestContextExtractor = requestContextExtractor;
    }

    @PostMapping
    public ResponseEntity<AssetResponse> create(
        @Valid @RequestBody AssetCreateRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        Asset asset = assetApplicationService.create(
            tenantId,
            request.assetTag(),
            request.serialNumber(),
            request.assetType(),
            request.status(),
            request.assignedUserId(),
            request.locationId(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return ResponseEntity
            .created(URI.create("/api/v1/assets/" + asset.id()))
            .body(AssetResponse.from(asset));
    }

    @GetMapping
    public List<AssetResponse> list(HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        return assetApplicationService.findAll(tenantId).stream()
            .map(AssetResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    public AssetResponse get(@PathVariable UUID id, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        return AssetResponse.from(assetApplicationService.findById(tenantId, id));
    }

    @PatchMapping("/{id}")
    public AssetResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody AssetPatchRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        return AssetResponse.from(assetApplicationService.update(
            tenantId,
            id,
            request.assetTag(),
            request.serialNumber(),
            request.assetType(),
            request.status(),
            request.touchesAssignedUserId(),
            request.touchesLocationId(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        ));
    }

    @PostMapping("/{id}/assign")
    public AssignAssetResponse assign(
        @PathVariable UUID id,
        @Valid @RequestBody AssignAssetRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        AssetResponsibilityApplicationService.AssignResult result = assetResponsibilityApplicationService.assign(
            tenantId,
            id,
            request.targetUserId(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return new AssignAssetResponse(result.id(), result.assignedUserId());
    }

    @PostMapping("/{id}/transfer")
    public TransferAssetResponse transfer(
        @PathVariable UUID id,
        @Valid @RequestBody TransferAssetRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        AssetResponsibilityApplicationService.TransferResult result = assetResponsibilityApplicationService.transfer(
            tenantId,
            id,
            request.targetUserId(),
            request.targetLocationId(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return new TransferAssetResponse(result.id(), result.assignedUserId(), result.locationId());
    }

    @PostMapping("/{id}/return")
    public ReturnAssetResponse returnAsset(
        @PathVariable UUID id,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        AssetResponsibilityApplicationService.ReturnResult result = assetResponsibilityApplicationService.returnAsset(
            tenantId,
            id,
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return new ReturnAssetResponse(result.id());
    }

    @PostMapping("/{id}/retire")
    public RetireAssetResponse retire(
        @PathVariable UUID id,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        AssetResponsibilityApplicationService.RetireResult result = assetResponsibilityApplicationService.retire(
            tenantId,
            id,
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return new RetireAssetResponse(result.id());
    }

    public record AssetCreateRequest(
        @NotBlank String assetTag,
        @NotBlank String serialNumber,
        @NotBlank String assetType,
        @NotBlank String status,
        UUID assignedUserId,
        UUID locationId
    ) {
    }

    public record AssetPatchRequest(
        @NotBlank String assetTag,
        @NotBlank String serialNumber,
        @NotBlank String assetType,
        @NotBlank String status,
        JsonNode assignedUserId,
        JsonNode locationId
    ) {
        public boolean touchesAssignedUserId() {
            return assignedUserId != null;
        }

        public boolean touchesLocationId() {
            return locationId != null;
        }
    }

    public record AssetResponse(
        UUID id,
        String assetTag,
        String serialNumber,
        String assetType,
        String status,
        UUID assignedUserId,
        UUID locationId
    ) {

        private static AssetResponse from(Asset asset) {
            return new AssetResponse(
                asset.id(),
                asset.assetTag(),
                asset.serialNumber(),
                asset.assetType(),
                asset.status(),
                asset.assignedUserId(),
                asset.locationId()
            );
        }
    }

    public record AssignAssetRequest(@NotNull UUID targetUserId) {
    }

    public record TransferAssetRequest(UUID targetUserId, UUID targetLocationId) {
    }

    public record AssignAssetResponse(UUID id, UUID assignedUserId) {
    }

    public record TransferAssetResponse(UUID id, UUID assignedUserId, UUID locationId) {
    }

    public record ReturnAssetResponse(UUID id) {
    }

    public record RetireAssetResponse(UUID id) {
    }
}
