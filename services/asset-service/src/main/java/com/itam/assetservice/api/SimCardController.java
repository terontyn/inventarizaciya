package com.itam.assetservice.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.itam.assetservice.application.AssetAuthorizationService;
import com.itam.assetservice.domain.SimCard;
import com.itam.assetservice.infrastructure.JdbcSimCardRepository;
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
@RequestMapping("/api/v1/sim-cards")
public class SimCardController {

    private final JdbcSimCardRepository repository;
    private final AuthenticatedRequestContextExtractor extractor;
    private final AssetAuthorizationService authorizationService;

    public SimCardController(
        JdbcSimCardRepository repository,
        AuthenticatedRequestContextExtractor extractor,
        AssetAuthorizationService authorizationService
    ) {
        this.repository = repository;
        this.extractor = extractor;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public List<SimCard> list(HttpServletRequest request) {
        UUID tenantId = extractor.requireTenantId(request);
        UUID userId = extractor.requireAuthenticatedUserId(request);
        authorizationService.requireAssetReadAccess(tenantId, userId);
        return repository.findAll(tenantId);
    }

    @PostMapping
    public SimCard create(@RequestBody SimCardUpsertPayload payload, HttpServletRequest request) {
        UUID tenantId = extractor.requireTenantId(request);
        UUID userId = extractor.requireAuthenticatedUserId(request);
        authorizationService.requireAssetMutateAccess(tenantId, userId);
        if (payload.phoneNumber() == null || payload.phoneNumber().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "phoneNumber is required");
        }
        if (payload.operator() == null || payload.operator().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "operator is required");
        }
        return repository.save(new SimCard(
            UUID.randomUUID(),
            tenantId,
            payload.phoneNumber().strip(),
            payload.operator().strip(),
            payload.tariff(),
            payload.issuedToName(),
            payload.locationName(),
            payload.status() != null ? payload.status() : "ACTIVE"
        ));
    }

    @PatchMapping("/{id}")
    public SimCard update(@PathVariable UUID id, @RequestBody SimCardUpsertPayload payload, HttpServletRequest request) {
        UUID tenantId = extractor.requireTenantId(request);
        UUID userId = extractor.requireAuthenticatedUserId(request);
        authorizationService.requireAssetMutateAccess(tenantId, userId);
        SimCard existing = repository.findById(tenantId, id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SimCard not found"));
        return repository.save(new SimCard(
            existing.id(),
            existing.tenantId(),
            payload.phoneNumber() != null ? payload.phoneNumber() : existing.phoneNumber(),
            payload.operator() != null ? payload.operator() : existing.operator(),
            payload.tariff() != null ? payload.tariff() : existing.tariff(),
            payload.issuedToName() != null ? payload.issuedToName() : existing.issuedToName(),
            payload.locationName() != null ? payload.locationName() : existing.locationName(),
            payload.status() != null ? payload.status() : existing.status()
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, HttpServletRequest request) {
        UUID tenantId = extractor.requireTenantId(request);
        UUID userId = extractor.requireAuthenticatedUserId(request);
        authorizationService.requireAssetMutateAccess(tenantId, userId);
        repository.findById(tenantId, id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SimCard not found"));
        repository.delete(tenantId, id);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SimCardUpsertPayload(
        String phoneNumber,
        String operator,
        String tariff,
        String issuedToName,
        String locationName,
        String status
    ) {}
}
