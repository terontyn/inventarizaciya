package com.itam.userservice.api;

import com.itam.userservice.application.UserApplicationService;
import com.itam.userservice.domain.User;
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
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserApplicationService userApplicationService;
    private final AuthenticatedRequestContextExtractor requestContextExtractor;

    public UserController(
        UserApplicationService userApplicationService,
        AuthenticatedRequestContextExtractor requestContextExtractor
    ) {
        this.userApplicationService = userApplicationService;
        this.requestContextExtractor = requestContextExtractor;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(
        @Valid @RequestBody UserUpsertRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
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
        return userApplicationService.findAll(tenantId).stream()
            .map(UserResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable UUID id, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        return UserResponse.from(userApplicationService.findById(tenantId, id));
    }

    @PatchMapping("/{id}")
    public UserResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody UserUpsertRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        return UserResponse.from(userApplicationService.update(
            tenantId,
            id,
            request.name(),
            request.departmentId(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        ));
    }

    public record UserUpsertRequest(@NotBlank String name, UUID departmentId) {
    }

    public record UserResponse(UUID id, String name, UUID departmentId) {

        private static UserResponse from(User user) {
            return new UserResponse(user.id(), user.name(), user.departmentId());
        }
    }
}
