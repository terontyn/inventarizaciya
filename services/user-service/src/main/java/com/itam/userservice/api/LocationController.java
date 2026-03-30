package com.itam.userservice.api;

import com.itam.userservice.application.LocationApplicationService;
import com.itam.userservice.domain.Location;
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
@RequestMapping("/api/v1/locations")
public class LocationController {

    private final LocationApplicationService locationApplicationService;
    private final AuthenticatedRequestContextExtractor requestContextExtractor;

    public LocationController(
        LocationApplicationService locationApplicationService,
        AuthenticatedRequestContextExtractor requestContextExtractor
    ) {
        this.locationApplicationService = locationApplicationService;
        this.requestContextExtractor = requestContextExtractor;
    }

    @PostMapping
    public ResponseEntity<LocationResponse> create(
        @Valid @RequestBody LocationUpsertRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        Location location = locationApplicationService.create(
            tenantId,
            request.name(),
            request.branchId(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return ResponseEntity
            .created(URI.create("/api/v1/locations/" + location.id()))
            .body(LocationResponse.from(location));
    }

    @GetMapping
    public List<LocationResponse> list(HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        return locationApplicationService.findAll(tenantId).stream()
            .map(LocationResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    public LocationResponse get(@PathVariable UUID id, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        return LocationResponse.from(locationApplicationService.findById(tenantId, id));
    }

    @PatchMapping("/{id}")
    public LocationResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody LocationUpsertRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        return LocationResponse.from(locationApplicationService.update(
            tenantId,
            id,
            request.name(),
            request.branchId(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        ));
    }

    public record LocationUpsertRequest(@NotBlank String name, @NotNull UUID branchId) {
    }

    public record LocationResponse(UUID id, String name, UUID branchId) {

        private static LocationResponse from(Location location) {
            return new LocationResponse(location.id(), location.name(), location.branchId());
        }
    }
}
