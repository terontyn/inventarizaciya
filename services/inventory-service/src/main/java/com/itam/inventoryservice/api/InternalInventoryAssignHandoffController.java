package com.itam.inventoryservice.api;

import com.itam.inventoryservice.application.InventoryApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/internal/api/v1/inventory/assets")
public class InternalInventoryAssignHandoffController {

    private final InventoryApplicationService inventoryApplicationService;
    private final AuthenticatedRequestContextExtractor requestContextExtractor;

    public InternalInventoryAssignHandoffController(
        InventoryApplicationService inventoryApplicationService,
        AuthenticatedRequestContextExtractor requestContextExtractor
    ) {
        this.inventoryApplicationService = inventoryApplicationService;
        this.requestContextExtractor = requestContextExtractor;
    }

    @PostMapping("/{assetId}/assign-handoff")
    public ResponseEntity<AssignHandoffResponse> consumeAssignHandoff(
        @PathVariable UUID assetId,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        InventoryApplicationService.AssignHandoffResult result = inventoryApplicationService.consumeAssignHandoff(
            tenantId,
            assetId,
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return ResponseEntity.accepted()
            .body(new AssignHandoffResponse(result.assetId(), result.consumed()));
    }

    public record AssignHandoffResponse(UUID assetId, boolean consumed) {
    }
}
