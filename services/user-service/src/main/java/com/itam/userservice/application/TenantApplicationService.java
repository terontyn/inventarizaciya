package com.itam.userservice.application;

import com.itam.userservice.domain.AuditAction;
import com.itam.userservice.domain.AuditEntity;
import com.itam.userservice.domain.Tenant;
import com.itam.userservice.domain.TenantRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TenantApplicationService {

    private final TenantRepository tenantRepository;
    private final AuditWriter auditWriter;

    public TenantApplicationService(TenantRepository tenantRepository, AuditWriter auditWriter) {
        this.tenantRepository = tenantRepository;
        this.auditWriter = auditWriter;
    }

    @Transactional
    public Tenant create(String name, AuditMetadata auditMetadata) {
        String normalizedName = InputNormalizer.requiredTrimmed(name, "name");
        Tenant tenant = tenantRepository.create(normalizedName);
        auditWriter.record(
            tenant.id(),
            auditMetadata,
            AuditEntity.TENANT,
            tenant.id(),
            AuditAction.CREATE,
            new TenantAuditPayload(tenant.id(), tenant.name())
        );
        return tenant;
    }

    public Tenant getById(UUID id, Optional<UUID> authenticatedTenantId) {
        if (authenticatedTenantId.isPresent() && !authenticatedTenantId.get().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authenticated tenant mismatch");
        }

        return tenantRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found"));
    }

    private record TenantAuditPayload(UUID id, String name) {
    }
}
