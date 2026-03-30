package com.itam.userservice.api;

import com.itam.userservice.application.DepartmentApplicationService;
import com.itam.userservice.domain.Department;
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
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentApplicationService departmentApplicationService;
    private final AuthenticatedRequestContextExtractor requestContextExtractor;

    public DepartmentController(
        DepartmentApplicationService departmentApplicationService,
        AuthenticatedRequestContextExtractor requestContextExtractor
    ) {
        this.departmentApplicationService = departmentApplicationService;
        this.requestContextExtractor = requestContextExtractor;
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> create(
        @Valid @RequestBody DepartmentUpsertRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        Department department = departmentApplicationService.create(
            tenantId,
            request.name(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return ResponseEntity
            .created(URI.create("/api/v1/departments/" + department.id()))
            .body(DepartmentResponse.from(department));
    }

    @GetMapping
    public List<DepartmentResponse> list(HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        return departmentApplicationService.findAll(tenantId).stream()
            .map(DepartmentResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    public DepartmentResponse get(@PathVariable UUID id, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        return DepartmentResponse.from(departmentApplicationService.findById(tenantId, id));
    }

    @PatchMapping("/{id}")
    public DepartmentResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody DepartmentUpsertRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        return DepartmentResponse.from(departmentApplicationService.update(
            tenantId,
            id,
            request.name(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        ));
    }

    public record DepartmentUpsertRequest(@NotBlank String name) {
    }

    public record DepartmentResponse(UUID id, String name) {

        private static DepartmentResponse from(Department department) {
            return new DepartmentResponse(department.id(), department.name());
        }
    }
}
