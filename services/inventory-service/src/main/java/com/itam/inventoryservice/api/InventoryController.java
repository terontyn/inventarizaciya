package com.itam.inventoryservice.api;

import com.itam.inventoryservice.application.InventoryApplicationService;
import com.itam.inventoryservice.application.InventoryAuthorizationService;
import com.itam.inventoryservice.domain.InventoryRecord;
import com.itam.inventoryservice.domain.StoragePlace;
import com.itam.inventoryservice.domain.Warehouse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
public class InventoryController {

    private final InventoryApplicationService inventoryApplicationService;
    private final InventoryAuthorizationService inventoryAuthorizationService;
    private final AuthenticatedRequestContextExtractor requestContextExtractor;

    public InventoryController(
        InventoryApplicationService inventoryApplicationService,
        InventoryAuthorizationService inventoryAuthorizationService,
        AuthenticatedRequestContextExtractor requestContextExtractor
    ) {
        this.inventoryApplicationService = inventoryApplicationService;
        this.inventoryAuthorizationService = inventoryAuthorizationService;
        this.requestContextExtractor = requestContextExtractor;
    }

    @GetMapping("/api/v1/warehouses")
    public List<WarehouseResponse> listWarehouses(HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        inventoryAuthorizationService.requireInventoryReadAccess(tenantId, authenticatedUserId);
        return inventoryApplicationService.findAllWarehouses(tenantId).stream()
            .map(WarehouseResponse::from)
            .toList();
    }

    @GetMapping("/api/v1/warehouses/{id}/storage-places")
    public List<StoragePlaceResponse> listStoragePlaces(@PathVariable UUID id, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        inventoryAuthorizationService.requireInventoryReadAccess(tenantId, authenticatedUserId);
        return inventoryApplicationService.findStoragePlaces(tenantId, id).stream()
            .map(StoragePlaceResponse::from)
            .toList();
    }

    @GetMapping("/api/v1/inventory/assets/in-storage")
    public List<InventoryAssetResponse> listAssetsInStorage(HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        inventoryAuthorizationService.requireInventoryReadAccess(tenantId, authenticatedUserId);
        return inventoryApplicationService.findAssetsInStorage(tenantId).stream()
            .map(InventoryAssetResponse::fromCurrentRecord)
            .toList();
    }

    @GetMapping("/api/v1/inventory/assets/{assetId}")
    public InventoryAssetResponse getAssetInventoryView(@PathVariable UUID assetId, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        inventoryAuthorizationService.requireInventoryReadAccess(tenantId, authenticatedUserId);
        return InventoryAssetResponse.fromView(
            inventoryApplicationService.findAssetView(tenantId, authenticatedUserId, assetId)
        );
    }

    @PostMapping("/api/v1/inventory/assets/{assetId}/place-in-storage")
    public ResponseEntity<InventoryAssetResponse> placeInStorage(
        @PathVariable UUID assetId,
        @Valid @RequestBody PlaceInStorageRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        inventoryAuthorizationService.requireInventoryMutateAccess(tenantId, authenticatedUserId);
        InventoryApplicationService.InventoryAssetView result = inventoryApplicationService.placeInStorage(
            tenantId,
            authenticatedUserId,
            assetId,
            request.warehouseId(),
            request.storagePlaceId(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return ResponseEntity
            .created(URI.create("/api/v1/inventory/assets/" + assetId))
            .body(InventoryAssetResponse.fromView(result));
    }

    @PostMapping("/api/v1/inventory/assets/{assetId}/move")
    public InventoryAssetResponse move(
        @PathVariable UUID assetId,
        @Valid @RequestBody MoveInStorageRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        inventoryAuthorizationService.requireInventoryMutateAccess(tenantId, authenticatedUserId);
        return InventoryAssetResponse.fromView(inventoryApplicationService.move(
            tenantId,
            authenticatedUserId,
            assetId,
            request.warehouseId(),
            request.storagePlaceId(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        ));
    }

    public record WarehouseResponse(UUID id) {

        private static WarehouseResponse from(Warehouse warehouse) {
            return new WarehouseResponse(warehouse.id());
        }
    }

    public record StoragePlaceResponse(UUID id, UUID warehouseId) {

        private static StoragePlaceResponse from(StoragePlace storagePlace) {
            return new StoragePlaceResponse(storagePlace.id(), storagePlace.warehouseId());
        }
    }

    public record InventoryAssetResponse(
        UUID assetId,
        boolean inStorageNow,
        UUID warehouseId,
        UUID storagePlaceId
    ) {

        private static InventoryAssetResponse fromCurrentRecord(InventoryRecord inventoryRecord) {
            return new InventoryAssetResponse(
                inventoryRecord.assetId(),
                true,
                inventoryRecord.warehouseId(),
                inventoryRecord.storagePlaceId()
            );
        }

        private static InventoryAssetResponse fromView(InventoryApplicationService.InventoryAssetView view) {
            return new InventoryAssetResponse(
                view.assetId(),
                view.inStorageNow(),
                view.warehouseId(),
                view.storagePlaceId()
            );
        }
    }

    public record PlaceInStorageRequest(@NotNull UUID warehouseId, UUID storagePlaceId) {
    }

    public record MoveInStorageRequest(UUID warehouseId, UUID storagePlaceId) {
    }
}
