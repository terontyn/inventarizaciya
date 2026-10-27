package com.itam.assetservice.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.itam.assetservice.application.AssetAuthorizationService;
import com.itam.assetservice.domain.SupportRequest;
import com.itam.assetservice.domain.SupportRequest.SupportRequestStatus;
import com.itam.assetservice.domain.SupportRequestRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Validated
@RequestMapping("/api/v1/requests")
public class SupportRequestController {

    /**
     * Статусы, которые может выставить только администратор.
     * Сотрудник не может одобрить или закрыть свою собственную заявку.
     */
    private static final Set<SupportRequestStatus> ADMIN_ONLY_STATUSES = Set.of(
        SupportRequestStatus.APPROVED,
        SupportRequestStatus.RESOLVED
    );

    private final SupportRequestRepository repository;
    private final AuthenticatedRequestContextExtractor requestContextExtractor;
    private final AssetAuthorizationService authorizationService;

    public SupportRequestController(
        SupportRequestRepository repository,
        AuthenticatedRequestContextExtractor requestContextExtractor,
        AssetAuthorizationService authorizationService
    ) {
        this.repository = repository;
        this.requestContextExtractor = requestContextExtractor;
        this.authorizationService = authorizationService;
    }

    @PostMapping
    public ResponseEntity<SupportRequest> create(
        @Valid @RequestBody CreateRequestPayload payload,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);

        SupportRequest req = new SupportRequest(
            UUID.randomUUID(),
            tenantId,
            authenticatedUserId,
            payload.userName(),
            payload.type(),
            payload.description(),
            SupportRequestStatus.NEW,
            null,
            OffsetDateTime.now(ZoneOffset.UTC)
        );

        repository.save(req);

        return ResponseEntity
            .created(URI.create("/api/v1/requests/" + req.id()))
            .body(req);
    }

    @GetMapping
    public List<SupportRequest> list(HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);

        boolean isAdmin = resolveIsAdmin(tenantId, authenticatedUserId);

        List<SupportRequest> allRequests = repository.findByTenantId(tenantId);

        if (isAdmin) {
            return allRequests;
        } else {
            return allRequests.stream()
                .filter(req -> req.userId().equals(authenticatedUserId))
                .toList();
        }
    }

    @PatchMapping("/{id}/resolve")
    public SupportRequest resolve(
        @PathVariable UUID id,
        @Valid @RequestBody ResolveRequestPayload payload,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);

        // Validate status early — valueOf throws 500, we need 400
        SupportRequestStatus newStatus = parseStatus(payload.status());

        SupportRequest req = repository.findById(tenantId, id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        boolean isAdmin = resolveIsAdmin(tenantId, authenticatedUserId);

        // Access check: non-admin can only update their own request
        if (!isAdmin && !req.userId().equals(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        // Role check: only admin can set privileged statuses
        if (!isAdmin && ADMIN_ONLY_STATUSES.contains(newStatus)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only administrators can set status " + newStatus.name()
            );
        }

        // description is immutable after creation — employee comments go to resolutionComment
        String newResComment = payload.resolutionComment() != null
            ? payload.resolutionComment()
            : req.resolutionComment();

        SupportRequest resolved = new SupportRequest(
            req.id(),
            req.tenantId(),
            req.userId(),
            req.userName(),
            req.type(),
            req.description(),  // immutable
            newStatus,
            newResComment,
            req.createdAt()
        );

        return repository.save(resolved);
    }

    /**
     * Проверяет, имеет ли пользователь роль администратора.
     * Пробрасывает все исключения кроме FORBIDDEN (403) — например, 503 при недоступности user-service.
     */
    private boolean resolveIsAdmin(UUID tenantId, UUID authenticatedUserId) {
        try {
            authorizationService.requireAssetMutateAccess(tenantId, authenticatedUserId);
            return true;
        } catch (ResponseStatusException rse) {
            if (HttpStatus.FORBIDDEN.equals(rse.getStatusCode())) {
                // Not admin — expected, continue as employee
                return false;
            }
            // 503 и прочие ошибки — пробрасываем, не глотаем
            throw rse;
        }
    }

    /**
     * Разбирает строковый статус и возвращает 400, если он неизвестен (не 500).
     */
    private static SupportRequestStatus parseStatus(String rawStatus) {
        try {
            return SupportRequestStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException | NullPointerException e) {
            String allowed = Arrays.stream(SupportRequestStatus.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid status: '" + rawStatus + "'. Allowed values: " + allowed
            );
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreateRequestPayload(
        @NotBlank String userName,
        @NotBlank String type,
        @NotBlank String description
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResolveRequestPayload(
        @NotBlank String status,
        String resolutionComment
    ) {}
}
