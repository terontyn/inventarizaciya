package com.itam.userservice.api;

import com.itam.userservice.application.PilotAuthorizationService;
import com.itam.userservice.application.RoleApplicationService;
import com.itam.userservice.domain.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleApplicationService roleApplicationService;
    private final PilotAuthorizationService pilotAuthorizationService;
    private final AuthenticatedRequestContextExtractor requestContextExtractor;

    public RoleController(
        RoleApplicationService roleApplicationService,
        PilotAuthorizationService pilotAuthorizationService,
        AuthenticatedRequestContextExtractor requestContextExtractor
    ) {
        this.roleApplicationService = roleApplicationService;
        this.pilotAuthorizationService = pilotAuthorizationService;
        this.requestContextExtractor = requestContextExtractor;
    }

    @PostMapping
    public ResponseEntity<RoleResponse> create(
        @Valid @RequestBody RoleUpsertRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        pilotAuthorizationService.requireTenantAdmin(tenantId, authenticatedUserId);
        Role role = roleApplicationService.create(
            tenantId,
            request.name(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return ResponseEntity
            .created(URI.create("/api/v1/roles/" + role.id()))
            .body(RoleResponse.from(role));
    }

    @GetMapping
    public List<RoleResponse> list(HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        pilotAuthorizationService.requireTenantAdmin(tenantId, authenticatedUserId);
        return roleApplicationService.findAll(tenantId).stream()
            .map(RoleResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    public RoleResponse get(@PathVariable UUID id, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        pilotAuthorizationService.requireTenantAdmin(tenantId, authenticatedUserId);
        return RoleResponse.from(roleApplicationService.findById(tenantId, id));
    }

    @PatchMapping("/{id}")
    public RoleResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody RoleUpsertRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        pilotAuthorizationService.requireTenantAdmin(tenantId, authenticatedUserId);
        return RoleResponse.from(roleApplicationService.update(
            tenantId,
            id,
            request.name(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        ));
    }

    public record RoleUpsertRequest(@NotBlank String name) {
    }

    public record RoleResponse(UUID id, String name) {

        private static RoleResponse from(Role role) {
            return new RoleResponse(role.id(), role.name());
        }
    }
}
