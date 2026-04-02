package com.itam.assetservice.application;

import com.itam.assetservice.domain.Asset;
import com.itam.assetservice.domain.AssetRepository;
import com.itam.assetservice.domain.AuditAction;
import com.itam.assetservice.domain.AuditEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssetApplicationService {

    private final AssetRepository assetRepository;
    private final AssetReferenceValidator assetReferenceValidator;
    private final AuditWriter auditWriter;

    public AssetApplicationService(
        AssetRepository assetRepository,
        AssetReferenceValidator assetReferenceValidator,
        AuditWriter auditWriter
    ) {
        this.assetRepository = assetRepository;
        this.assetReferenceValidator = assetReferenceValidator;
        this.auditWriter = auditWriter;
    }

    @Transactional
    public Asset create(
        UUID tenantId,
        UUID authenticatedUserId,
        String assetTag,
        String serialNumber,
        String assetType,
        String status,
        UUID assignedUserId,
        UUID locationId,
        AuditMetadata auditMetadata
    ) {
        String normalizedAssetTag = InputNormalizer.requiredTrimmed(assetTag, "asset_tag");
        String normalizedSerialNumber = InputNormalizer.requiredTrimmed(serialNumber, "serial_number");
        String normalizedAssetType = InputNormalizer.requiredTrimmed(assetType, "asset_type");
        String normalizedStatus = InputNormalizer.requiredTrimmed(status, "status");

        assetReferenceValidator.requireUserInSameTenant(
            tenantId,
            authenticatedUserId,
            assignedUserId,
            "assigned_user_id"
        );
        assetReferenceValidator.requireLocationInSameTenant(
            tenantId,
            authenticatedUserId,
            locationId,
            "location_id"
        );

        try {
            Asset asset = assetRepository.create(
                tenantId,
                normalizedAssetTag,
                normalizedSerialNumber,
                normalizedAssetType,
                normalizedStatus,
                assignedUserId,
                locationId
            );
            auditWriter.record(
                tenantId,
                auditMetadata,
                AuditEntity.ASSET,
                asset.id(),
                AuditAction.CREATE,
                new AssetAuditPayload(
                    asset.id(),
                    asset.assetTag(),
                    asset.serialNumber(),
                    asset.assetType(),
                    asset.status(),
                    asset.assignedUserId(),
                    asset.locationId()
                )
            );
            return asset;
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "asset_tag must be unique within tenant",
                exception
            );
        }
    }

    public List<Asset> findAll(UUID tenantId) {
        return assetRepository.findAll(tenantId);
    }

    public Asset findById(UUID tenantId, UUID id) {
        return assetRepository.findById(tenantId, id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));
    }

    @Transactional
    public Asset update(
        UUID tenantId,
        UUID id,
        String assetTag,
        String serialNumber,
        String assetType,
        String status,
        boolean touchesAssignedUserId,
        boolean touchesLocationId,
        AuditMetadata auditMetadata
    ) {
        if (touchesAssignedUserId || touchesLocationId) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Generic PATCH must not modify assigned_user_id or location_id"
            );
        }

        String normalizedAssetTag = InputNormalizer.requiredTrimmed(assetTag, "asset_tag");
        String normalizedSerialNumber = InputNormalizer.requiredTrimmed(serialNumber, "serial_number");
        String normalizedAssetType = InputNormalizer.requiredTrimmed(assetType, "asset_type");
        String normalizedStatus = InputNormalizer.requiredTrimmed(status, "status");

        try {
            Asset asset = assetRepository.update(
                tenantId,
                id,
                normalizedAssetTag,
                normalizedSerialNumber,
                normalizedAssetType,
                normalizedStatus
            ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));

            auditWriter.record(
                tenantId,
                auditMetadata,
                AuditEntity.ASSET,
                asset.id(),
                AuditAction.UPDATE,
                new AssetAuditPayload(
                    asset.id(),
                    asset.assetTag(),
                    asset.serialNumber(),
                    asset.assetType(),
                    asset.status(),
                    asset.assignedUserId(),
                    asset.locationId()
                )
            );
            return asset;
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "asset_tag must be unique within tenant",
                exception
            );
        }
    }

    private record AssetAuditPayload(
        UUID id,
        String assetTag,
        String serialNumber,
        String assetType,
        String status,
        UUID assignedUserId,
        UUID locationId
    ) {
    }
}
