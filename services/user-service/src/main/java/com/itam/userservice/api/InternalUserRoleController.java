package com.itam.userservice.api;

import com.itam.userservice.application.PilotAuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/api/v1/users")
public class InternalUserRoleController {

    private final PilotAuthorizationService pilotAuthorizationService;
    private final AuthenticatedRequestContextExtractor requestContextExtractor;

    public InternalUserRoleController(
        PilotAuthorizationService pilotAuthorizationService,
        AuthenticatedRequestContextExtractor requestContextExtractor
    ) {
        this.pilotAuthorizationService = pilotAuthorizationService;
        this.requestContextExtractor = requestContextExtractor;
    }

    @GetMapping("/{userId}/roles")
    public RoleResolutionResponse getUserRoles(
        @PathVariable UUID userId,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        return new RoleResolutionResponse(
            userId,
            pilotAuthorizationService.findRoleNames(tenantId, userId)
        );
    }

    public record RoleResolutionResponse(UUID userId, List<String> roleNames) {
    }
}
