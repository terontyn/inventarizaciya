package com.itam.assetservice.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.itam.assetservice.application.AssetAuthorizationService;
import com.itam.assetservice.domain.Cartridge;
import com.itam.assetservice.infrastructure.JdbcCartridgeRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/cartridges")
public class CartridgeController {

    private final JdbcCartridgeRepository repository;
    private final AuthenticatedRequestContextExtractor extractor;
    private final AssetAuthorizationService authorizationService;

    public CartridgeController(
        JdbcCartridgeRepository repository,
        AuthenticatedRequestContextExtractor extractor,
        AssetAuthorizationService authorizationService
    ) {
        this.repository = repository;
        this.extractor = extractor;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public List<Cartridge> list(HttpServletRequest request) {
        UUID tenantId = extractor.requireTenantId(request);
        UUID userId = extractor.requireAuthenticatedUserId(request);
        authorizationService.requireAssetReadAccess(tenantId, userId);
        return repository.findAll(tenantId);
    }

    @PostMapping
    public Cartridge create(@RequestBody CartridgeUpsertPayload payload, HttpServletRequest request) {
        UUID tenantId = extractor.requireTenantId(request);
        UUID userId = extractor.requireAuthenticatedUserId(request);
        authorizationService.requireAssetMutateAccess(tenantId, userId);
        if (payload.model() == null || payload.model().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "model is required");
        }
        if (payload.compatibility() == null || payload.compatibility().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "compatibility is required");
        }
        return repository.save(new Cartridge(
            UUID.randomUUID(),
            tenantId,
            payload.model().strip(),
            payload.compatibility().strip(),
            payload.locationId(),
            payload.status() != null ? payload.status() : "FULL",
            payload.printerAssetId()
        ));
    }

    @PatchMapping("/{id}")
    public Cartridge update(@PathVariable UUID id, @RequestBody CartridgeUpsertPayload payload, HttpServletRequest request) {
        UUID tenantId = extractor.requireTenantId(request);
        UUID userId = extractor.requireAuthenticatedUserId(request);
        authorizationService.requireAssetMutateAccess(tenantId, userId);
        Cartridge existing = repository.findById(tenantId, id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cartridge not found"));
        return repository.save(new Cartridge(
            existing.id(),
            existing.tenantId(),
            payload.model() != null ? payload.model() : existing.model(),
            payload.compatibility() != null ? payload.compatibility() : existing.compatibility(),
            payload.locationId() != null ? payload.locationId() : existing.locationId(),
            payload.status() != null ? payload.status() : existing.status(),
            payload.printerAssetId() != null ? payload.printerAssetId() : existing.printerAssetId()
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, HttpServletRequest request) {
        UUID tenantId = extractor.requireTenantId(request);
        UUID userId = extractor.requireAuthenticatedUserId(request);
        authorizationService.requireAssetMutateAccess(tenantId, userId);
        repository.findById(tenantId, id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cartridge not found"));
        repository.delete(tenantId, id);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CartridgeUpsertPayload(
        String model,
        String compatibility,
        UUID locationId,
        String status,
        UUID printerAssetId
    ) {}
}
