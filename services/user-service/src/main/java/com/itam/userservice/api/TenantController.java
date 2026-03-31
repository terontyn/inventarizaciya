package com.itam.userservice.api;

import com.itam.userservice.application.TenantApplicationService;
import com.itam.userservice.domain.Tenant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantApplicationService tenantApplicationService;
    private final AuthenticatedRequestContextExtractor requestContextExtractor;

    public TenantController(
        TenantApplicationService tenantApplicationService,
        AuthenticatedRequestContextExtractor requestContextExtractor
    ) {
        this.tenantApplicationService = tenantApplicationService;
        this.requestContextExtractor = requestContextExtractor;
    }

    @PostMapping
    public ResponseEntity<TenantResponse> create(
        @Valid @RequestBody CreateTenantRequest request,
        HttpServletRequest servletRequest
    ) {
        Tenant tenant = tenantApplicationService.create(
            request.name(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return ResponseEntity
            .created(URI.create("/api/v1/tenants/" + tenant.id()))
            .body(TenantResponse.from(tenant));
    }

    @GetMapping("/{id}")
    public TenantResponse get(@PathVariable UUID id, HttpServletRequest servletRequest) {
        return TenantResponse.from(
            tenantApplicationService.getById(id, requestContextExtractor.requireTenantId(servletRequest))
        );
    }

    public record CreateTenantRequest(@NotBlank String name) {
    }

    public record TenantResponse(UUID id, String name) {

        private static TenantResponse from(Tenant tenant) {
            return new TenantResponse(tenant.id(), tenant.name());
        }
    }
}
