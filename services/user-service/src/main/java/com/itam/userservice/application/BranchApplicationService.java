package com.itam.userservice.application;

import com.itam.userservice.domain.AuditAction;
import com.itam.userservice.domain.AuditEntity;
import com.itam.userservice.domain.Branch;
import com.itam.userservice.domain.BranchRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BranchApplicationService {

    private final BranchRepository branchRepository;
    private final AuditWriter auditWriter;

    public BranchApplicationService(BranchRepository branchRepository, AuditWriter auditWriter) {
        this.branchRepository = branchRepository;
        this.auditWriter = auditWriter;
    }

    @Transactional
    public Branch create(UUID tenantId, String name, AuditMetadata auditMetadata) {
        String normalizedName = InputNormalizer.requiredTrimmed(name, "name");
        Branch branch = branchRepository.create(tenantId, normalizedName);
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.BRANCH,
            branch.id(),
            AuditAction.CREATE,
            new NamedReferenceAuditPayload(branch.id(), branch.name())
        );
        return branch;
    }

    public List<Branch> findAll(UUID tenantId) {
        return branchRepository.findAll(tenantId);
    }

    public Branch findById(UUID tenantId, UUID id) {
        return branchRepository.findById(tenantId, id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));
    }

    @Transactional
    public Branch update(UUID tenantId, UUID id, String name, AuditMetadata auditMetadata) {
        String normalizedName = InputNormalizer.requiredTrimmed(name, "name");
        Branch branch = branchRepository.update(tenantId, id, normalizedName)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.BRANCH,
            branch.id(),
            AuditAction.UPDATE,
            new NamedReferenceAuditPayload(branch.id(), branch.name())
        );
        return branch;
    }

    private record NamedReferenceAuditPayload(UUID id, String name) {
    }
}
