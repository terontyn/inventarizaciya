package com.itam.inventoryservice.application;

import com.itam.inventoryservice.domain.AuditAction;
import com.itam.inventoryservice.domain.AuditEntity;
import com.itam.inventoryservice.domain.InventoryRecord;
import com.itam.inventoryservice.domain.InventoryRecordRepository;
import com.itam.inventoryservice.domain.StoragePlace;
import com.itam.inventoryservice.domain.StoragePlaceRepository;
import com.itam.inventoryservice.domain.Warehouse;
import com.itam.inventoryservice.domain.WarehouseRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventoryApplicationService {

    private final WarehouseRepository warehouseRepository;
    private final StoragePlaceRepository storagePlaceRepository;
    private final InventoryRecordRepository inventoryRecordRepository;
    private final AssetServiceAssetLookup assetServiceAssetLookup;
    private final AuditWriter auditWriter;

    public InventoryApplicationService(
        WarehouseRepository warehouseRepository,
        StoragePlaceRepository storagePlaceRepository,
        InventoryRecordRepository inventoryRecordRepository,
        AssetServiceAssetLookup assetServiceAssetLookup,
        AuditWriter auditWriter
    ) {
        this.warehouseRepository = warehouseRepository;
        this.storagePlaceRepository = storagePlaceRepository;
        this.inventoryRecordRepository = inventoryRecordRepository;
        this.assetServiceAssetLookup = assetServiceAssetLookup;
        this.auditWriter = auditWriter;
    }

    public List<Warehouse> findAllWarehouses(UUID tenantId) {
        return warehouseRepository.findAll(tenantId);
    }

    public List<StoragePlace> findStoragePlaces(UUID tenantId, UUID warehouseId) {
        requireWarehouse(tenantId, warehouseId);
        return storagePlaceRepository.findByWarehouseId(tenantId, warehouseId);
    }

    public List<InventoryRecord> findAssetsInStorage(UUID tenantId) {
        return inventoryRecordRepository.findAllCurrent(tenantId);
    }

    public InventoryAssetView findAssetView(UUID tenantId, UUID authenticatedUserId, UUID assetId) {
        requireTrackedAsset(tenantId, authenticatedUserId, assetId);
        return toInventoryAssetView(assetId, inventoryRecordRepository.findByAssetId(tenantId, assetId).orElse(null));
    }

    @Transactional
    public InventoryAssetView placeInStorage(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID assetId,
        UUID warehouseId,
        UUID storagePlaceId,
        AuditMetadata auditMetadata
    ) {
        requireTrackedAsset(tenantId, authenticatedUserId, assetId);
        Warehouse warehouse = requireWarehouse(tenantId, warehouseId);
        StoragePlace storagePlace = requireStoragePlaceIfPresent(tenantId, storagePlaceId);
        ensureStoragePlaceBelongsToWarehouse(storagePlace, warehouse);

        if (inventoryRecordRepository.findByAssetId(tenantId, assetId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset is already in storage custody");
        }

        try {
            InventoryRecord inventoryRecord = inventoryRecordRepository.create(
                tenantId,
                assetId,
                warehouse.id(),
                storagePlace == null ? null : storagePlace.id()
            );
            auditWriter.record(
                tenantId,
                auditMetadata,
                AuditEntity.INVENTORY_RECORD,
                inventoryRecord.id(),
                AuditAction.PLACE_IN_STORAGE,
                new InventoryPlacementAuditPayload(
                    inventoryRecord.assetId(),
                    inventoryRecord.warehouseId(),
                    inventoryRecord.storagePlaceId()
                )
            );
            return toInventoryAssetView(assetId, inventoryRecord);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset is already in storage custody", exception);
        }
    }

    @Transactional
    public InventoryAssetView move(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID assetId,
        UUID warehouseId,
        UUID storagePlaceId,
        AuditMetadata auditMetadata
    ) {
        requireTrackedAsset(tenantId, authenticatedUserId, assetId);
        InventoryRecord currentRecord = inventoryRecordRepository.findByAssetId(tenantId, assetId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Asset is not currently in storage custody"
            ));

        if (warehouseId == null && storagePlaceId == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Move requires warehouse_id or storage_place_id"
            );
        }

        StoragePlace targetStoragePlace = requireStoragePlaceIfPresent(tenantId, storagePlaceId);
        UUID resolvedWarehouseId = warehouseId == null
            ? targetStoragePlace.warehouseId()
            : warehouseId;
        Warehouse targetWarehouse = requireWarehouse(tenantId, resolvedWarehouseId);
        ensureStoragePlaceBelongsToWarehouse(targetStoragePlace, targetWarehouse);
        UUID resolvedStoragePlaceId = targetStoragePlace == null ? null : targetStoragePlace.id();

        if (equalsUuid(currentRecord.warehouseId(), targetWarehouse.id())
            && equalsUuid(currentRecord.storagePlaceId(), resolvedStoragePlaceId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Move must change warehouse_id or storage_place_id"
            );
        }

        InventoryRecord movedRecord = inventoryRecordRepository.updatePlacement(
            tenantId,
            assetId,
            targetWarehouse.id(),
            resolvedStoragePlaceId
        ).orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Asset is not currently in storage custody"
        ));

        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.INVENTORY_RECORD,
            movedRecord.id(),
            AuditAction.MOVE,
            new MoveAuditPayload(
                movedRecord.assetId(),
                currentRecord.warehouseId(),
                currentRecord.storagePlaceId(),
                movedRecord.warehouseId(),
                movedRecord.storagePlaceId()
            )
        );

        return toInventoryAssetView(assetId, movedRecord);
    }

    @Transactional
    public AssignHandoffResult consumeAssignHandoff(UUID tenantId, UUID assetId, AuditMetadata auditMetadata) {
        InventoryRecord currentRecord = inventoryRecordRepository.findByAssetId(tenantId, assetId).orElse(null);
        if (currentRecord == null) {
            return new AssignHandoffResult(assetId, false);
        }

        inventoryRecordRepository.deleteById(tenantId, currentRecord.id());
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.INVENTORY_RECORD,
            currentRecord.id(),
            AuditAction.CONSUME_FROM_ASSIGN,
            new InventoryPlacementAuditPayload(
                currentRecord.assetId(),
                currentRecord.warehouseId(),
                currentRecord.storagePlaceId()
            )
        );
        return new AssignHandoffResult(assetId, true);
    }

    private void requireTrackedAsset(UUID tenantId, UUID authenticatedUserId, UUID assetId) {
        try {
            boolean exists = assetServiceAssetLookup.assetExists(tenantId, authenticatedUserId, assetId);
            if (!exists) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found");
            }
        } catch (ReferenceLookupUnavailableException exception) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Unable to validate asset via asset-service",
                exception
            );
        }
    }

    private Warehouse requireWarehouse(UUID tenantId, UUID warehouseId) {
        return warehouseRepository.findById(tenantId, warehouseId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Warehouse not found"));
    }

    private StoragePlace requireStoragePlaceIfPresent(UUID tenantId, UUID storagePlaceId) {
        if (storagePlaceId == null) {
            return null;
        }
        return storagePlaceRepository.findById(tenantId, storagePlaceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Storage place not found"));
    }

    private void ensureStoragePlaceBelongsToWarehouse(StoragePlace storagePlace, Warehouse warehouse) {
        if (storagePlace != null && !storagePlace.warehouseId().equals(warehouse.id())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "storage_place_id must belong to warehouse_id"
            );
        }
    }

    private InventoryAssetView toInventoryAssetView(UUID assetId, InventoryRecord inventoryRecord) {
        if (inventoryRecord == null) {
            return new InventoryAssetView(assetId, false, null, null);
        }
        return new InventoryAssetView(
            assetId,
            true,
            inventoryRecord.warehouseId(),
            inventoryRecord.storagePlaceId()
        );
    }

    private boolean equalsUuid(UUID left, UUID right) {
        return left == null ? right == null : left.equals(right);
    }

    public record InventoryAssetView(
        UUID assetId,
        boolean inStorageNow,
        UUID warehouseId,
        UUID storagePlaceId
    ) {
    }

    public record AssignHandoffResult(UUID assetId, boolean consumed) {
    }

    private record InventoryPlacementAuditPayload(UUID assetId, UUID warehouseId, UUID storagePlaceId) {
    }

    private record MoveAuditPayload(
        UUID assetId,
        UUID previousWarehouseId,
        UUID previousStoragePlaceId,
        UUID newWarehouseId,
        UUID newStoragePlaceId
    ) {
    }
}
