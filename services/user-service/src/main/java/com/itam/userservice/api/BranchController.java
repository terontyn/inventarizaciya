package com.itam.userservice.api;

import com.itam.userservice.application.BranchApplicationService;
import com.itam.userservice.application.PilotAuthorizationService;
import com.itam.userservice.domain.Branch;
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
@RequestMapping("/api/v1/branches")
public class BranchController {

    private final BranchApplicationService branchApplicationService;
    private final PilotAuthorizationService pilotAuthorizationService;
    private final AuthenticatedRequestContextExtractor requestContextExtractor;

    public BranchController(
        BranchApplicationService branchApplicationService,
        PilotAuthorizationService pilotAuthorizationService,
        AuthenticatedRequestContextExtractor requestContextExtractor
    ) {
        this.branchApplicationService = branchApplicationService;
        this.pilotAuthorizationService = pilotAuthorizationService;
        this.requestContextExtractor = requestContextExtractor;
    }

    @PostMapping
    public ResponseEntity<BranchResponse> create(
        @Valid @RequestBody BranchUpsertRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        pilotAuthorizationService.requireTenantAdmin(tenantId, authenticatedUserId);
        Branch branch = branchApplicationService.create(
            tenantId,
            request.name(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return ResponseEntity
            .created(URI.create("/api/v1/branches/" + branch.id()))
            .body(BranchResponse.from(branch));
    }

    @GetMapping
    public List<BranchResponse> list(HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        pilotAuthorizationService.requireReferenceRead(tenantId, authenticatedUserId);
        return branchApplicationService.findAll(tenantId).stream()
            .map(BranchResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    public BranchResponse get(@PathVariable UUID id, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        pilotAuthorizationService.requireReferenceRead(tenantId, authenticatedUserId);
        return BranchResponse.from(branchApplicationService.findById(tenantId, id));
    }

    @PatchMapping("/{id}")
    public BranchResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody BranchUpsertRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        pilotAuthorizationService.requireTenantAdmin(tenantId, authenticatedUserId);
        return BranchResponse.from(branchApplicationService.update(
            tenantId,
            id,
            request.name(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        ));
    }

    public record BranchUpsertRequest(@NotBlank String name) {
    }

    public record BranchResponse(UUID id, String name) {

        private static BranchResponse from(Branch branch) {
            return new BranchResponse(branch.id(), branch.name());
        }
    }
}
