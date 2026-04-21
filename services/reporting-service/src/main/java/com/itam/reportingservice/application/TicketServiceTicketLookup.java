package com.itam.reportingservice.application;

import java.util.UUID;

public interface TicketServiceTicketLookup {

    DerivedSliceResult<TicketSlice> findAssetTickets(UUID tenantId, UUID authenticatedUserId, UUID assetId);

    record TicketSlice(UUID assetId, Boolean hasOpenTickets) {
    }
}
