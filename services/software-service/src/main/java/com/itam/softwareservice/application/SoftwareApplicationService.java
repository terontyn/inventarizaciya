package com.itam.softwareservice.application;

import com.itam.softwareservice.domain.AuditAction;
import com.itam.softwareservice.domain.AuditEntity;
import com.itam.softwareservice.domain.SoftwareAssetAllocation;
import com.itam.softwareservice.domain.SoftwareAssetAllocationRepository;
import com.itam.softwareservice.domain.SoftwareRecord;
import com.itam.softwareservice.domain.SoftwareRecordRepository;
import com.itam.softwareservice.domain.SoftwareUserAllocation;
import com.itam.softwareservice.domain.SoftwareUserAllocationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SoftwareApplicationService {

    private final SoftwareRecordRepository softwareRecordRepository;
    private final SoftwareUserAllocationRepository softwareUserAllocationRepository;
    private final SoftwareAssetAllocationRepository softwareAssetAllocationRepository;
    private final UserServiceUserLookup userServiceUserLookup;
    private final AssetServiceAssetLookup assetServiceAssetLookup;
    private final AuditWriter auditWriter;

    public SoftwareApplicationService(
        SoftwareRecordRepository softwareRecordRepository,
        SoftwareUserAllocationRepository softwareUserAllocationRepository,
        SoftwareAssetAllocationRepository softwareAssetAllocationRepository,
        UserServiceUserLookup userServiceUserLookup,
        AssetServiceAssetLookup assetServiceAssetLookup,
        AuditWriter auditWriter
    ) {
        this.softwareRecordRepository = softwareRecordRepository;
        this.softwareUserAllocationRepository = softwareUserAllocationRepository;
        this.softwareAssetAllocationRepository = softwareAssetAllocationRepository;
        this.userServiceUserLookup = userServiceUserLookup;
        this.assetServiceAssetLookup = assetServiceAssetLookup;
        this.auditWriter = auditWriter;
    }

    public List<SoftwareRecord> findAllRecords(UUID tenantId) {
        return softwareRecordRepository.findAll(tenantId);
    }

    public SoftwareRecordView findRecord(UUID tenantId, UUID recordId) {
        SoftwareRecord record = requireRecord(tenantId, recordId);
        return toRecordView(tenantId, record);
    }

    public List<SoftwareRecord> findSoftwareForUser(UUID tenantId, UUID authenticatedUserId, UUID userId) {
        requireTrackedUser(tenantId, authenticatedUserId, userId);
        List<UUID> recordIds = softwareUserAllocationRepository.findByUserId(tenantId, userId).stream()
            .map(SoftwareUserAllocation::softwareRecordId)
            .distinct()
            .toList();
        return softwareRecordRepository.findByIds(tenantId, recordIds);
    }

    public List<SoftwareRecord> findSoftwareForAsset(UUID tenantId, UUID authenticatedUserId, UUID assetId) {
        requireTrackedAsset(tenantId, authenticatedUserId, assetId);
        List<UUID> recordIds = softwareAssetAllocationRepository.findByAssetId(tenantId, assetId).stream()
            .map(SoftwareAssetAllocation::softwareRecordId)
            .distinct()
            .toList();
        return softwareRecordRepository.findByIds(tenantId, recordIds);
    }

    @Transactional
    public SoftwareRecordView createRecord(UUID tenantId, String name, AuditMetadata auditMetadata) {
        SoftwareRecord record = softwareRecordRepository.create(tenantId, name);
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.SOFTWARE_RECORD,
            record.id(),
            AuditAction.CREATE_SOFTWARE_RECORD,
            new SoftwareRecordAuditPayload(record.name())
        );
        return toRecordView(tenantId, record);
    }

    @Transactional
    public SoftwareRecordView allocateToUser(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID recordId,
        UUID userId,
        AuditMetadata auditMetadata
    ) {
        SoftwareRecord record = requireRecord(tenantId, recordId);
        requireTrackedUser(tenantId, authenticatedUserId, userId);
        if (softwareUserAllocationRepository.findBySoftwareRecordIdAndUserId(tenantId, recordId, userId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Current software-to-user relation already exists");
        }
        try {
            SoftwareUserAllocation allocation = softwareUserAllocationRepository.create(tenantId, recordId, userId);
            auditWriter.record(
                tenantId,
                auditMetadata,
                AuditEntity.SOFTWARE_USER_ALLOCATION,
                allocation.id(),
                AuditAction.ALLOCATE_TO_USER,
                new UserAllocationAuditPayload(record.id(), allocation.userId())
            );
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Current software-to-user relation already exists",
                exception
            );
        }
        return toRecordView(tenantId, record);
    }

    @Transactional
    public SoftwareRecordView allocateToAsset(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID recordId,
        UUID assetId,
        AuditMetadata auditMetadata
    ) {
        SoftwareRecord record = requireRecord(tenantId, recordId);
        requireTrackedAsset(tenantId, authenticatedUserId, assetId);
        if (softwareAssetAllocationRepository.findBySoftwareRecordIdAndAssetId(tenantId, recordId, assetId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Current software-to-asset relation already exists");
        }
        try {
            SoftwareAssetAllocation allocation = softwareAssetAllocationRepository.create(tenantId, recordId, assetId);
            auditWriter.record(
                tenantId,
                auditMetadata,
                AuditEntity.SOFTWARE_ASSET_ALLOCATION,
                allocation.id(),
                AuditAction.ALLOCATE_TO_ASSET,
                new AssetAllocationAuditPayload(record.id(), allocation.assetId())
            );
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Current software-to-asset relation already exists",
                exception
            );
        }
        return toRecordView(tenantId, record);
    }

    @Transactional
    public SoftwareRecordView removeFromUser(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID recordId,
        UUID userId,
        AuditMetadata auditMetadata
    ) {
        SoftwareRecord record = requireRecord(tenantId, recordId);
        requireTrackedUser(tenantId, authenticatedUserId, userId);
        SoftwareUserAllocation allocation = softwareUserAllocationRepository.findBySoftwareRecordIdAndUserId(
            tenantId,
            recordId,
            userId
        ).orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Current software-to-user relation not found"
        ));
        softwareUserAllocationRepository.deleteById(tenantId, allocation.id());
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.SOFTWARE_USER_ALLOCATION,
            allocation.id(),
            AuditAction.REMOVE_FROM_USER,
            new UserAllocationAuditPayload(record.id(), allocation.userId())
        );
        return toRecordView(tenantId, record);
    }

    @Transactional
    public SoftwareRecordView removeFromAsset(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID recordId,
        UUID assetId,
        AuditMetadata auditMetadata
    ) {
        SoftwareRecord record = requireRecord(tenantId, recordId);
        requireTrackedAsset(tenantId, authenticatedUserId, assetId);
        SoftwareAssetAllocation allocation = softwareAssetAllocationRepository.findBySoftwareRecordIdAndAssetId(
            tenantId,
            recordId,
            assetId
        ).orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Current software-to-asset relation not found"
        ));
        softwareAssetAllocationRepository.deleteById(tenantId, allocation.id());
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.SOFTWARE_ASSET_ALLOCATION,
            allocation.id(),
            AuditAction.REMOVE_FROM_ASSET,
            new AssetAllocationAuditPayload(record.id(), allocation.assetId())
        );
        return toRecordView(tenantId, record);
    }

    private SoftwareRecord requireRecord(UUID tenantId, UUID recordId) {
        return softwareRecordRepository.findById(tenantId, recordId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Software record not found"));
    }

    private void requireTrackedUser(UUID tenantId, UUID authenticatedUserId, UUID userId) {
        try {
            boolean exists = userServiceUserLookup.userExists(tenantId, authenticatedUserId, userId);
            if (!exists) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
            }
        } catch (ReferenceLookupUnavailableException exception) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Unable to validate user via user-service",
                exception
            );
        }
    }

    private void requireTrackedAsset(UUID tenantId, UUID authenticatedUserId, UUID assetId) {
        try {
            boolean exists = assetServiceAssetLookup.assetExists(tenantId, authenticatedUserId, assetId);
            if (!exists) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found");
            }
        } catch (ReferenceLookupUnavailableException exception) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Unable to validate asset via asset-service",
                exception
            );
        }
    }

    private SoftwareRecordView toRecordView(UUID tenantId, SoftwareRecord record) {
        return new SoftwareRecordView(
            record.id(),
            record.name(),
            softwareUserAllocationRepository.findBySoftwareRecordId(tenantId, record.id()).stream()
                .map(SoftwareUserAllocation::userId)
                .toList(),
            softwareAssetAllocationRepository.findBySoftwareRecordId(tenantId, record.id()).stream()
                .map(SoftwareAssetAllocation::assetId)
                .toList()
        );
    }

    public record SoftwareRecordView(
        UUID id,
        String name,
        List<UUID> allocatedUserIds,
        List<UUID> allocatedAssetIds
    ) {
    }

    private record SoftwareRecordAuditPayload(String name) {
    }

    private record UserAllocationAuditPayload(UUID softwareRecordId, UUID userId) {
    }

    private record AssetAllocationAuditPayload(UUID softwareRecordId, UUID assetId) {
    }
}
