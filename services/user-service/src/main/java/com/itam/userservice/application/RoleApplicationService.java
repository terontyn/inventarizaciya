package com.itam.userservice.application;

import com.itam.userservice.domain.AuditAction;
import com.itam.userservice.domain.AuditEntity;
import com.itam.userservice.domain.Role;
import com.itam.userservice.domain.RoleRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RoleApplicationService {

    private final RoleRepository roleRepository;
    private final AuditWriter auditWriter;

    public RoleApplicationService(
        RoleRepository roleRepository,
        AuditWriter auditWriter
    ) {
        this.roleRepository = roleRepository;
        this.auditWriter = auditWriter;
    }

    @Transactional
    public Role create(UUID tenantId, String name, AuditMetadata auditMetadata) {
        String normalizedName = InputNormalizer.requiredTrimmed(name, "name");
        Role role = roleRepository.create(tenantId, normalizedName);
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.ROLE,
            role.id(),
            AuditAction.CREATE,
            new RoleAuditPayload(role.id(), role.name())
        );
        return role;
    }

    public List<Role> findAll(UUID tenantId) {
        return roleRepository.findAll(tenantId);
    }

    public Role findById(UUID tenantId, UUID id) {
        return roleRepository.findById(tenantId, id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
    }

    @Transactional
    public Role update(UUID tenantId, UUID id, String name, AuditMetadata auditMetadata) {
        String normalizedName = InputNormalizer.requiredTrimmed(name, "name");
        Role role = roleRepository.update(tenantId, id, normalizedName)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.ROLE,
            role.id(),
            AuditAction.UPDATE,
            new RoleAuditPayload(role.id(), role.name())
        );
        return role;
    }

    private record RoleAuditPayload(UUID id, String name) {
    }
}
