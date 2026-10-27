package com.itam.reportingservice.application;

import com.itam.reportingservice.domain.AssetOperationalCurrentView;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReportingApplicationService {

    private final AssetServiceAssetLookup assetServiceAssetLookup;
    private final InventoryServiceInventoryLookup inventoryServiceInventoryLookup;
    private final MaintenanceServiceMaintenanceLookup maintenanceServiceMaintenanceLookup;
    private final TicketServiceTicketLookup ticketServiceTicketLookup;
    private final SoftwareServiceSoftwareLookup softwareServiceSoftwareLookup;

    public ReportingApplicationService(
        AssetServiceAssetLookup assetServiceAssetLookup,
        InventoryServiceInventoryLookup inventoryServiceInventoryLookup,
        MaintenanceServiceMaintenanceLookup maintenanceServiceMaintenanceLookup,
        TicketServiceTicketLookup ticketServiceTicketLookup,
        SoftwareServiceSoftwareLookup softwareServiceSoftwareLookup
    ) {
        this.assetServiceAssetLookup = assetServiceAssetLookup;
        this.inventoryServiceInventoryLookup = inventoryServiceInventoryLookup;
        this.maintenanceServiceMaintenanceLookup = maintenanceServiceMaintenanceLookup;
        this.ticketServiceTicketLookup = ticketServiceTicketLookup;
        this.softwareServiceSoftwareLookup = softwareServiceSoftwareLookup;
    }

    public List<AssetOperationalCurrentView> findAllAssets(UUID tenantId, UUID authenticatedUserId) {
        try {
            return assetServiceAssetLookup.findAllAssets(tenantId, authenticatedUserId).stream()
                .map(asset -> composeCurrentView(tenantId, authenticatedUserId, asset))
                .toList();
        } catch (ReferenceLookupUnavailableException exception) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Unable to read assets via asset-service",
                exception
            );
        }
    }

    public AssetOperationalCurrentView findAsset(UUID tenantId, UUID authenticatedUserId, UUID assetId) {
        try {
            AssetServiceAssetLookup.AssetSummary asset = assetServiceAssetLookup.findAsset(
                tenantId,
                authenticatedUserId,
                assetId
            ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));
            return composeCurrentView(tenantId, authenticatedUserId, asset);
        } catch (ReferenceLookupUnavailableException exception) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Unable to read asset via asset-service",
                exception
            );
        }
    }

    private AssetOperationalCurrentView composeCurrentView(
        UUID tenantId,
        UUID authenticatedUserId,
        AssetServiceAssetLookup.AssetSummary asset
    ) {
        DerivedSliceResult<InventoryServiceInventoryLookup.InventorySlice> inventorySlice =
            inventoryServiceInventoryLookup.findAssetInventory(tenantId, authenticatedUserId, asset.assetId());
        DerivedSliceResult<MaintenanceServiceMaintenanceLookup.MaintenanceSlice> maintenanceSlice =
            maintenanceServiceMaintenanceLookup.findAssetMaintenance(tenantId, authenticatedUserId, asset.assetId());
        DerivedSliceResult<TicketServiceTicketLookup.TicketSlice> ticketSlice =
            ticketServiceTicketLookup.findAssetTickets(tenantId, authenticatedUserId, asset.assetId());
        DerivedSliceResult<SoftwareServiceSoftwareLookup.SoftwareSlice> softwareSlice =
            softwareServiceSoftwareLookup.findAssetSoftware(tenantId, authenticatedUserId, asset.assetId());

        InventoryServiceInventoryLookup.InventorySlice inventory = inventorySlice.value();
        MaintenanceServiceMaintenanceLookup.MaintenanceSlice maintenance = maintenanceSlice.value();
        TicketServiceTicketLookup.TicketSlice tickets = ticketSlice.value();
        SoftwareServiceSoftwareLookup.SoftwareSlice software = softwareSlice.value();

        return new AssetOperationalCurrentView(
            asset.assetId(),
            asset.assignedUserId(),
            asset.assignedLocationId(),
            inventorySlice.available() ? inventory.warehouseId() : null,
            inventorySlice.available() ? inventory.storagePlaceId() : null,
            inventorySlice.available() ? inventory.inStorageNow() : null,
            inventorySlice.available() ? inventory.readinessState() : null,
            maintenanceSlice.available() ? maintenance.hasOpenMaintenanceWork() : null,
            ticketSlice.available() ? tickets.hasOpenTickets() : null,
            softwareSlice.available() ? software.softwareRelations() : List.of(),
            inventorySlice.available(),
            maintenanceSlice.available(),
            ticketSlice.available(),
            softwareSlice.available()
        );
    }
}
