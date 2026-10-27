package com.itam.assetservice.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.itam.assetservice.application.AssetAuthorizationService;
import com.itam.assetservice.domain.AssetType;
import com.itam.assetservice.infrastructure.JdbcAssetTypeRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Validated
@RequestMapping("/api/v1/asset-types")
public class AssetTypeController {

    private final JdbcAssetTypeRepository repository;
    private final AuthenticatedRequestContextExtractor extractor;
    private final AssetAuthorizationService authorizationService;

    public AssetTypeController(
        JdbcAssetTypeRepository repository,
        AuthenticatedRequestContextExtractor extractor,
        AssetAuthorizationService authorizationService
    ) {
        this.repository = repository;
        this.extractor = extractor;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public List<AssetType> list(HttpServletRequest request) {
        UUID tenantId = extractor.requireTenantId(request);
        UUID userId = extractor.requireAuthenticatedUserId(request);
        authorizationService.requireAssetReadAccess(tenantId, userId);
        return repository.findAll(tenantId);
    }

    @PostMapping
    public AssetType create(
        @Valid @RequestBody AssetTypeCreateRequest body,
        HttpServletRequest request
    ) {
        UUID tenantId = extractor.requireTenantId(request);
        UUID userId = extractor.requireAuthenticatedUserId(request);
        // Only admins may define new asset types
        authorizationService.requireAssetMutateAccess(tenantId, userId);

        String name = body.name().strip();
        if (name.length() > 128) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "name must not exceed 128 characters");
        }

        return repository.save(new AssetType(UUID.randomUUID(), tenantId, name, body.attributes()));
    }

    /** Request DTO — domain-объект AssetType не передаётся напрямую из API. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AssetTypeCreateRequest(
        @NotBlank String name,
        String attributes
    ) {}
}
