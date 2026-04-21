package com.itam.reportingservice.domain;

import java.util.List;
import java.util.UUID;

public record AssetOperationalCurrentView(
    UUID assetId,
    UUID assignedUserId,
    UUID assignedLocationId,
    UUID warehouseId,
    UUID storagePlaceId,
    Boolean inStorageNow,
    String readinessState,
    Boolean hasOpenMaintenanceWork,
    Boolean hasOpenTickets,
    List<SoftwareRelationSummary> softwareRelations,
    boolean inventorySliceAvailable,
    boolean maintenanceSliceAvailable,
    boolean ticketSliceAvailable,
    boolean softwareSliceAvailable
) {

    public AssetOperationalCurrentView {
        softwareRelations = softwareRelations == null ? List.of() : List.copyOf(softwareRelations);
    }
}
