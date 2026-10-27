package com.itam.userservice.api;

import com.itam.userservice.application.PilotAuthorizationService;
import com.itam.userservice.application.UserApplicationService;
import com.itam.userservice.application.UserRoleApplicationService;
import com.itam.userservice.domain.User;
import com.itam.userservice.domain.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserApplicationService userApplicationService;
    private final UserRoleApplicationService userRoleApplicationService;
    private final PilotAuthorizationService pilotAuthorizationService;
    private final AuthenticatedRequestContextExtractor requestContextExtractor;

    public UserController(
        UserApplicationService userApplicationService,
        UserRoleApplicationService userRoleApplicationService,
        PilotAuthorizationService pilotAuthorizationService,
        AuthenticatedRequestContextExtractor requestContextExtractor
    ) {
        this.userApplicationService = userApplicationService;
        this.userRoleApplicationService = userRoleApplicationService;
        this.pilotAuthorizationService = pilotAuthorizationService;
        this.requestContextExtractor = requestContextExtractor;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(
        @Valid @RequestBody UserUpsertRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        pilotAuthorizationService.requireTenantAdmin(tenantId, authenticatedUserId);
        User user = userApplicationService.create(
            tenantId,
            request.name(),
            request.departmentId(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return ResponseEntity
            .created(URI.create("/api/v1/users/" + user.id()))
            .body(UserResponse.from(user));
    }

    @GetMapping
    public List<UserResponse> list(HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        pilotAuthorizationService.requireReferenceRead(tenantId, authenticatedUserId);
        return userApplicationService.findAll(tenantId).stream()
            .map(UserResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable UUID id, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        pilotAuthorizationService.requireReferenceRead(tenantId, authenticatedUserId);
        return UserResponse.from(userApplicationService.findById(tenantId, id));
    }

    @PatchMapping("/{id}")
    public UserResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody UserUpsertRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        pilotAuthorizationService.requireTenantAdmin(tenantId, authenticatedUserId);
        return UserResponse.from(userApplicationService.update(
            tenantId,
            id,
            request.name(),
            request.departmentId(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        pilotAuthorizationService.requireTenantAdmin(tenantId, authenticatedUserId);
        userApplicationService.delete(tenantId, id, requestContextExtractor.requireAuditMetadata(servletRequest));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles")
    public ResponseEntity<UserRoleBindingResponse> bindRole(
        @PathVariable UUID userId,
        @Valid @RequestBody UserRoleBindingRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        pilotAuthorizationService.requireTenantAdmin(tenantId, authenticatedUserId);
        UserRole userRole = userRoleApplicationService.bind(
            tenantId,
            userId,
            request.roleId(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new UserRoleBindingResponse(userRole.userId(), userRole.roleId()));
    }

    @GetMapping("/{userId}/roles")
    public List<String> getRoles(@PathVariable UUID userId, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        pilotAuthorizationService.requireReferenceRead(tenantId, authenticatedUserId);
        return userRoleApplicationService.getRoleNames(tenantId, userId);
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<Void> setRoles(
        @PathVariable UUID userId,
        @Valid @RequestBody UserRolesSetRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        pilotAuthorizationService.requireTenantAdmin(tenantId, authenticatedUserId);
        userRoleApplicationService.setRoles(
            tenantId,
            userId,
            request.roleIds(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return ResponseEntity.ok().build();
    }

    public record UserUpsertRequest(@NotBlank String name, UUID departmentId) {
    }

    public record UserRoleBindingRequest(@NotNull UUID roleId) {
    }

    public record UserRolesSetRequest(@NotNull List<UUID> roleIds) {
    }

    public record UserResponse(UUID id, String name, UUID departmentId) {

        private static UserResponse from(User user) {
            return new UserResponse(user.id(), user.name(), user.departmentId());
        }
    }

    public record UserRoleBindingResponse(UUID userId, UUID roleId) {
    }
}
