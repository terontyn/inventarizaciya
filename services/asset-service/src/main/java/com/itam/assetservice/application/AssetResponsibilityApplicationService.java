package com.itam.assetservice.application;

import com.itam.assetservice.domain.Asset;
import com.itam.assetservice.domain.AssetRepository;
import com.itam.assetservice.domain.AssetResponsibilityFact;
import com.itam.assetservice.domain.AssetResponsibilityFactRepository;
import com.itam.assetservice.domain.AssetResponsibilityFactType;
import com.itam.assetservice.domain.AuditAction;
import com.itam.assetservice.domain.AuditEntity;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssetResponsibilityApplicationService {

    private final AssetRepository assetRepository;
    private final AssetResponsibilityFactRepository assetResponsibilityFactRepository;
    private final AssetReferenceValidator assetReferenceValidator;
    private final AuditWriter auditWriter;
    private final Clock clock;

    public AssetResponsibilityApplicationService(
        AssetRepository assetRepository,
        AssetResponsibilityFactRepository assetResponsibilityFactRepository,
        AssetReferenceValidator assetReferenceValidator,
        AuditWriter auditWriter,
        Clock clock
    ) {
        this.assetRepository = assetRepository;
        this.assetResponsibilityFactRepository = assetResponsibilityFactRepository;
        this.assetReferenceValidator = assetReferenceValidator;
        this.auditWriter = auditWriter;
        this.clock = clock;
    }

    @Transactional
    public AssignResult assign(
        UUID tenantId,
        UUID assetId,
        UUID targetUserId,
        AuditMetadata auditMetadata
    ) {
        Asset asset = requireAsset(tenantId, assetId);
        assetReferenceValidator.requireUserInSameTenant(tenantId, targetUserId, "target_user_id");

        Asset updatedAsset = assetRepository.updateCurrentState(
            tenantId,
            assetId,
            targetUserId,
            asset.locationId()
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));

        appendFact(
            tenantId,
            asset,
            updatedAsset,
            AssetResponsibilityFactType.ASSIGN
        );

        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.ASSET,
            updatedAsset.id(),
            AuditAction.ASSIGN,
            new AssignAuditPayload(updatedAsset.id(), updatedAsset.assignedUserId())
        );

        return new AssignResult(updatedAsset.id(), updatedAsset.assignedUserId());
    }

    @Transactional
    public TransferResult transfer(
        UUID tenantId,
        UUID assetId,
        UUID targetUserId,
        UUID targetLocationId,
        AuditMetadata auditMetadata
    ) {
        Asset asset = requireAsset(tenantId, assetId);
        if (targetUserId == null && targetLocationId == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Transfer requires target_user_id or target_location_id"
            );
        }

        assetReferenceValidator.requireUserInSameTenant(tenantId, targetUserId, "target_user_id");
        assetReferenceValidator.requireLocationInSameTenant(tenantId, targetLocationId, "target_location_id");

        if (asset.assignedUserId() == null && targetUserId != null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Transfer cannot create the first current user responsibility; use assign"
            );
        }

        UUID newAssignedUserId = targetUserId == null ? asset.assignedUserId() : targetUserId;
        UUID newLocationId = targetLocationId == null ? asset.locationId() : targetLocationId;
        if (equalsUuid(asset.assignedUserId(), newAssignedUserId)
            && equalsUuid(asset.locationId(), newLocationId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Transfer must change assigned_user_id or location_id"
            );
        }

        Asset updatedAsset = assetRepository.updateCurrentState(
            tenantId,
            assetId,
            newAssignedUserId,
            newLocationId
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));

        appendFact(
            tenantId,
            asset,
            updatedAsset,
            AssetResponsibilityFactType.TRANSFER
        );

        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.ASSET,
            updatedAsset.id(),
            AuditAction.TRANSFER,
            new TransferAuditPayload(
                updatedAsset.id(),
                updatedAsset.assignedUserId(),
                updatedAsset.locationId()
            )
        );

        return new TransferResult(
            updatedAsset.id(),
            updatedAsset.assignedUserId(),
            updatedAsset.locationId()
        );
    }

    @Transactional
    public ReturnResult returnAsset(
        UUID tenantId,
        UUID assetId,
        AuditMetadata auditMetadata
    ) {
        Asset asset = requireAsset(tenantId, assetId);
        if (asset.assignedUserId() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Asset has no current active user responsibility to return"
            );
        }

        Asset updatedAsset = assetRepository.updateCurrentState(
            tenantId,
            assetId,
            null,
            asset.locationId()
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));

        appendFact(
            tenantId,
            asset,
            updatedAsset,
            AssetResponsibilityFactType.RETURN
        );

        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.ASSET,
            updatedAsset.id(),
            AuditAction.RETURN,
            new ReturnAuditPayload(updatedAsset.id())
        );

        return new ReturnResult(updatedAsset.id());
    }

    @Transactional
    public RetireResult retire(
        UUID tenantId,
        UUID assetId,
        AuditMetadata auditMetadata
    ) {
        Asset asset = requireAsset(tenantId, assetId);

        Asset updatedAsset = assetRepository.updateCurrentState(
            tenantId,
            assetId,
            null,
            asset.locationId()
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));

        appendFact(
            tenantId,
            asset,
            updatedAsset,
            AssetResponsibilityFactType.RETIRE
        );

        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.ASSET,
            updatedAsset.id(),
            AuditAction.RETIRE,
            new RetireAuditPayload(updatedAsset.id())
        );

        return new RetireResult(updatedAsset.id());
    }

    private Asset requireAsset(UUID tenantId, UUID assetId) {
        return assetRepository.findById(tenantId, assetId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));
    }

    private void appendFact(
        UUID tenantId,
        Asset previousAsset,
        Asset updatedAsset,
        AssetResponsibilityFactType factType
    ) {
        Instant occurredAt = Instant.now(clock);
        assetResponsibilityFactRepository.append(
            tenantId,
            new AssetResponsibilityFact(
                UUID.randomUUID(),
                tenantId,
                updatedAsset.id(),
                factType,
                previousAsset.assignedUserId(),
                updatedAsset.assignedUserId(),
                previousAsset.locationId(),
                updatedAsset.locationId(),
                occurredAt
            )
        );
    }

    private boolean equalsUuid(UUID left, UUID right) {
        return left == null ? right == null : left.equals(right);
    }

    public record AssignResult(UUID id, UUID assignedUserId) {
    }

    public record TransferResult(UUID id, UUID assignedUserId, UUID locationId) {
    }

    public record ReturnResult(UUID id) {
    }

    public record RetireResult(UUID id) {
    }

    private record AssignAuditPayload(UUID id, UUID assignedUserId) {
    }

    private record TransferAuditPayload(UUID id, UUID assignedUserId, UUID locationId) {
    }

    private record ReturnAuditPayload(UUID id) {
    }

    private record RetireAuditPayload(UUID id) {
    }
}
