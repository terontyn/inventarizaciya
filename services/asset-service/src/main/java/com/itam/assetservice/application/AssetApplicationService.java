package com.itam.assetservice.application;

import com.itam.assetservice.domain.Asset;
import com.itam.assetservice.domain.AssetRepository;
import com.itam.assetservice.domain.AssetStatus;
import com.itam.assetservice.domain.AuditAction;
import com.itam.assetservice.domain.AuditEntity;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssetApplicationService {

    private static final int DISPLAY_NAME_MAX_LENGTH = 160;
    private static final int NOTE_MAX_LENGTH = 2000;

    private final AssetRepository assetRepository;
    private final AssetReferenceValidator assetReferenceValidator;
    private final AuditWriter auditWriter;
    private final Clock clock;

    public AssetApplicationService(
        AssetRepository assetRepository,
        AssetReferenceValidator assetReferenceValidator,
        AuditWriter auditWriter,
        Clock clock
    ) {
        this.assetRepository = assetRepository;
        this.assetReferenceValidator = assetReferenceValidator;
        this.auditWriter = auditWriter;
        this.clock = clock;
    }

    @Transactional
    public Asset create(
        UUID tenantId,
        UUID authenticatedUserId,
        String assetTag,
        String serialNumber,
        String assetType,
        UUID assignedUserId,
        UUID locationId,
        String displayName,
        String note,
        UUID parentAssetId,
        String ipAddress,
        AuditMetadata auditMetadata
    ) {
        String normalizedAssetTag = InputNormalizer.requiredTrimmed(assetTag, "asset_tag");
        String normalizedSerialNumber = InputNormalizer.requiredTrimmed(serialNumber, "serial_number");
        String normalizedAssetType = InputNormalizer.requiredTrimmed(assetType, "asset_type");
        String normalizedDisplayName = normalizeDisplayName(displayName);
        String normalizedNote = normalizeNote(note);

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
        requireParentAssetInSameTenant(tenantId, parentAssetId);

        try {
            // QR payload is intentionally based on the immutable asset id only:
            // one code per asset, never changes even if tag/name/type are edited.
            UUID assetId = UUID.randomUUID();
            String qrData = "ITAM-ASSET:" + assetId;

            Asset asset = assetRepository.create(
                tenantId,
                assetId,
                normalizedAssetTag,
                normalizedSerialNumber,
                normalizedAssetType,
                assignedUserId,
                locationId,
                normalizedDisplayName,
                normalizedNote,
                qrData,
                parentAssetId,
                ipAddress
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
                    asset.locationId(),
                    asset.displayName(),
                    asset.note()
                )
            );
            return asset;
        } catch (DataIntegrityViolationException exception) {
            throw mapDataIntegrityViolation(exception);
        }
    }

    public List<Asset> findAll(UUID tenantId) {
        return assetRepository.findAll(tenantId);
    }

    public List<Asset> findAll(UUID tenantId, String visibility) {
        String normalizedVisibility = visibility == null ? "" : visibility.trim();
        if (normalizedVisibility.isEmpty()) {
            return assetRepository.findAll(tenantId);
        }
        if ("archived".equalsIgnoreCase(normalizedVisibility)) {
            return assetRepository.findArchived(tenantId);
        }
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "visibility supports only archived"
        );
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
        String displayName,
        boolean touchesDisplayName,
        String note,
        boolean touchesNote,
        boolean touchesAssignedUserId,
        boolean touchesLocationId,
        boolean touchesTechnicalAttributes,
        boolean touchesArchivedAt,
        AuditMetadata auditMetadata
    ) {
        if (touchesAssignedUserId || touchesLocationId || touchesTechnicalAttributes || touchesArchivedAt) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Generic PATCH must not modify assigned_user_id, location_id, technical_attributes, or archived_at"
            );
        }

        String normalizedAssetTag = InputNormalizer.requiredTrimmed(assetTag, "asset_tag");
        String normalizedSerialNumber = InputNormalizer.requiredTrimmed(serialNumber, "serial_number");
        String normalizedAssetType = InputNormalizer.requiredTrimmed(assetType, "asset_type");
        Asset currentAsset = findById(tenantId, id);
        String normalizedDisplayName = touchesDisplayName
            ? normalizeDisplayName(displayName)
            : currentAsset.displayName();
        String normalizedNote = touchesNote ? normalizeNote(note) : currentAsset.note();

        try {
            Asset asset = assetRepository.update(
                tenantId,
                id,
                normalizedAssetTag,
                normalizedSerialNumber,
                normalizedAssetType,
                normalizedDisplayName,
                normalizedNote
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
                    asset.locationId(),
                    asset.displayName(),
                    asset.note()
                )
            );
            return asset;
        } catch (DataIntegrityViolationException exception) {
            throw mapDataIntegrityViolation(exception);
        }
    }

    @Transactional
    public ArchiveResult archive(UUID tenantId, UUID id, AuditMetadata auditMetadata) {
        // Use FOR UPDATE to prevent race conditions on concurrent archive calls
        Asset asset = assetRepository.findByIdForUpdate(tenantId, id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));
        if (!AssetStatus.RETIRED.equalsIgnoreCase(asset.status())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Only retired assets can be archived"
            );
        }
        if (asset.archivedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset is already archived");
        }

        Instant archivedAt = Instant.now(clock);
        Asset archivedAsset = assetRepository.archive(tenantId, id, archivedAt)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Asset is already archived"));
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.ASSET,
            archivedAsset.id(),
            AuditAction.ARCHIVE,
            new AssetArchiveAuditPayload(
                archivedAsset.id(),
                archivedAsset.assetTag(),
                archivedAsset.status(),
                archivedAsset.assignedUserId(),
                archivedAsset.locationId(),
                archivedAsset.archivedAt()
            )
        );
        return new ArchiveResult(archivedAsset.id());
    }

    @Transactional
    public UnarchiveResult unarchive(UUID tenantId, UUID id, AuditMetadata auditMetadata) {
        // Use FOR UPDATE to prevent race conditions on concurrent unarchive calls
        Asset asset = assetRepository.findByIdForUpdate(tenantId, id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));
        if (!AssetStatus.RETIRED.equalsIgnoreCase(asset.status())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Only retired assets can be unarchived"
            );
        }
        if (asset.archivedAt() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset is not archived");
        }

        Instant previousArchivedAt = asset.archivedAt();
        Asset unarchivedAsset = assetRepository.unarchive(tenantId, id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Asset is not archived"));
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.ASSET,
            unarchivedAsset.id(),
            AuditAction.UNARCHIVE,
            new AssetUnarchiveAuditPayload(
                unarchivedAsset.id(),
                unarchivedAsset.assetTag(),
                unarchivedAsset.status(),
                unarchivedAsset.assignedUserId(),
                unarchivedAsset.locationId(),
                previousArchivedAt,
                unarchivedAsset.archivedAt()
            )
        );
        return new UnarchiveResult(unarchivedAsset.id());
    }

    private void requireParentAssetInSameTenant(UUID tenantId, UUID parentAssetId) {
        if (parentAssetId == null) {
            return;
        }
        assetRepository.findById(tenantId, parentAssetId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "parent_asset_id must reference an existing asset in the same tenant"
            ));
    }

    private ResponseStatusException mapDataIntegrityViolation(DataIntegrityViolationException exception) {
        String message = exception.getMostSpecificCause() == null
            ? ""
            : String.valueOf(exception.getMostSpecificCause().getMessage()).toLowerCase();
        if (message.contains("uq_assets_tenant_asset_tag")) {
            return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "asset_tag must be unique within tenant",
                exception
            );
        }
        if (message.contains("parent_asset_id")) {
            return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "parent_asset_id must reference an existing asset in the same tenant",
                exception
            );
        }
        return new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Request violates a data integrity constraint",
            exception
        );
    }

    private String normalizeDisplayName(String displayName) {
        return InputNormalizer.optionalTrimmed(
            displayName,
            "display_name",
            DISPLAY_NAME_MAX_LENGTH,
            true
        );
    }

    private String normalizeNote(String note) {
        return InputNormalizer.optionalTrimmed(
            note,
            "note",
            NOTE_MAX_LENGTH,
            false
        );
    }

    private record AssetAuditPayload(
        UUID id,
        String assetTag,
        String serialNumber,
        String assetType,
        String status,
        UUID assignedUserId,
        UUID locationId,
        String displayName,
        String note
    ) {
    }

    private record AssetArchiveAuditPayload(
        UUID id,
        String assetTag,
        String status,
        UUID assignedUserId,
        UUID locationId,
        Instant archivedAt
    ) {
    }

    private record AssetUnarchiveAuditPayload(
        UUID id,
        String assetTag,
        String status,
        UUID assignedUserId,
        UUID locationId,
        Instant previousArchivedAt,
        Instant archivedAt
    ) {
    }

    public record ArchiveResult(UUID id) {
    }

    public record UnarchiveResult(UUID id) {
    }
}
