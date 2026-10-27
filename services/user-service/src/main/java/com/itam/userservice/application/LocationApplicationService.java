package com.itam.userservice.application;

import com.itam.userservice.domain.AuditAction;
import com.itam.userservice.domain.AuditEntity;
import com.itam.userservice.domain.BranchRepository;
import com.itam.userservice.domain.Location;
import com.itam.userservice.domain.LocationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LocationApplicationService {

    private final LocationRepository locationRepository;
    private final BranchRepository branchRepository;
    private final AuditWriter auditWriter;

    public LocationApplicationService(
        LocationRepository locationRepository,
        BranchRepository branchRepository,
        AuditWriter auditWriter
    ) {
        this.locationRepository = locationRepository;
        this.branchRepository = branchRepository;
        this.auditWriter = auditWriter;
    }

    @Transactional
    public Location create(UUID tenantId, String name, UUID branchId, AuditMetadata auditMetadata) {
        String normalizedName = InputNormalizer.requiredTrimmed(name, "name");
        requireBranchInSameTenant(tenantId, branchId);
        Location location = locationRepository.create(tenantId, normalizedName, branchId);
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.LOCATION,
            location.id(),
            AuditAction.CREATE,
            new LocationAuditPayload(location.id(), location.name(), location.branchId())
        );
        return location;
    }

    public List<Location> findAll(UUID tenantId) {
        return locationRepository.findAll(tenantId);
    }

    public Location findById(UUID tenantId, UUID id) {
        return locationRepository.findById(tenantId, id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));
    }

    @Transactional
    public Location update(
        UUID tenantId,
        UUID id,
        String name,
        UUID branchId,
        AuditMetadata auditMetadata
    ) {
        String normalizedName = InputNormalizer.requiredTrimmed(name, "name");
        requireBranchInSameTenant(tenantId, branchId);
        Location location = locationRepository.update(tenantId, id, normalizedName, branchId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.LOCATION,
            location.id(),
            AuditAction.UPDATE,
            new LocationAuditPayload(location.id(), location.name(), location.branchId())
        );
        return location;
    }

    private void requireBranchInSameTenant(UUID tenantId, UUID branchId) {
        if (!branchRepository.exists(tenantId, branchId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "branch_id must reference a branch in the same tenant"
            );
        }
    }

    private record LocationAuditPayload(UUID id, String name, UUID branchId) {
    }
}
