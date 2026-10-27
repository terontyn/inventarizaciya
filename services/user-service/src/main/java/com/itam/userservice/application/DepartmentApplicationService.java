package com.itam.userservice.application;

import com.itam.userservice.domain.AuditAction;
import com.itam.userservice.domain.AuditEntity;
import com.itam.userservice.domain.Department;
import com.itam.userservice.domain.DepartmentRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DepartmentApplicationService {

    private final DepartmentRepository departmentRepository;
    private final AuditWriter auditWriter;

    public DepartmentApplicationService(
        DepartmentRepository departmentRepository,
        AuditWriter auditWriter
    ) {
        this.departmentRepository = departmentRepository;
        this.auditWriter = auditWriter;
    }

    @Transactional
    public Department create(UUID tenantId, String name, AuditMetadata auditMetadata) {
        String normalizedName = InputNormalizer.requiredTrimmed(name, "name");
        Department department = departmentRepository.create(tenantId, normalizedName);
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.DEPARTMENT,
            department.id(),
            AuditAction.CREATE,
            new NamedReferenceAuditPayload(department.id(), department.name())
        );
        return department;
    }

    public List<Department> findAll(UUID tenantId) {
        return departmentRepository.findAll(tenantId);
    }

    public Department findById(UUID tenantId, UUID id) {
        return departmentRepository.findById(tenantId, id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
    }

    @Transactional
    public Department update(UUID tenantId, UUID id, String name, AuditMetadata auditMetadata) {
        String normalizedName = InputNormalizer.requiredTrimmed(name, "name");
        Department department = departmentRepository.update(tenantId, id, normalizedName)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.DEPARTMENT,
            department.id(),
            AuditAction.UPDATE,
            new NamedReferenceAuditPayload(department.id(), department.name())
        );
        return department;
    }

    private record NamedReferenceAuditPayload(UUID id, String name) {
    }
}
