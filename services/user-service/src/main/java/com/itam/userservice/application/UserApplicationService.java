package com.itam.userservice.application;

import com.itam.userservice.domain.AuditAction;
import com.itam.userservice.domain.AuditEntity;
import com.itam.userservice.domain.DepartmentRepository;
import com.itam.userservice.domain.User;
import com.itam.userservice.domain.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserApplicationService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditWriter auditWriter;

    public UserApplicationService(
        UserRepository userRepository,
        DepartmentRepository departmentRepository,
        AuditWriter auditWriter
    ) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.auditWriter = auditWriter;
    }

    @Transactional
    public User create(UUID tenantId, String name, UUID departmentId, AuditMetadata auditMetadata) {
        String normalizedName = InputNormalizer.requiredTrimmed(name, "name");
        requireDepartmentInSameTenant(tenantId, departmentId);
        User user = userRepository.create(tenantId, normalizedName, departmentId);
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.USER,
            user.id(),
            AuditAction.CREATE,
            new UserAuditPayload(user.id(), user.name(), user.departmentId())
        );
        return user;
    }

    public List<User> findAll(UUID tenantId) {
        return userRepository.findAll(tenantId);
    }

    public User findById(UUID tenantId, UUID id) {
        return userRepository.findById(tenantId, id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional
    public User update(UUID tenantId, UUID id, String name, UUID departmentId, AuditMetadata auditMetadata) {
        String normalizedName = InputNormalizer.requiredTrimmed(name, "name");
        requireDepartmentInSameTenant(tenantId, departmentId);
        User user = userRepository.update(tenantId, id, normalizedName, departmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.USER,
            user.id(),
            AuditAction.UPDATE,
            new UserAuditPayload(user.id(), user.name(), user.departmentId())
        );
        return user;
    }

    private void requireDepartmentInSameTenant(UUID tenantId, UUID departmentId) {
        if (departmentId == null) {
            return;
        }
        if (!departmentRepository.exists(tenantId, departmentId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "department_id must reference a department in the same tenant"
            );
        }
    }

    @Transactional
    public void delete(UUID tenantId, UUID id, AuditMetadata auditMetadata) {
        User user = findById(tenantId, id);
        userRepository.delete(tenantId, id);
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.USER,
            user.id(),
            AuditAction.DELETE,
            new UserAuditPayload(user.id(), user.name(), user.departmentId())
        );
    }

    private record UserAuditPayload(UUID id, String name, UUID departmentId) {
    }
}
