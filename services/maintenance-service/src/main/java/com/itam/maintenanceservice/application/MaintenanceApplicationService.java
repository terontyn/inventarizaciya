package com.itam.maintenanceservice.application;

import com.itam.maintenanceservice.domain.AuditAction;
import com.itam.maintenanceservice.domain.AuditEntity;
import com.itam.maintenanceservice.domain.MaintenanceWork;
import com.itam.maintenanceservice.domain.MaintenanceWorkRepository;
import com.itam.maintenanceservice.domain.MaintenanceWorkState;
import com.itam.maintenanceservice.domain.MaintenanceWorkType;
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
public class MaintenanceApplicationService {

    private final MaintenanceWorkRepository maintenanceWorkRepository;
    private final AssetServiceAssetLookup assetServiceAssetLookup;
    private final AuditWriter auditWriter;
    private final Clock clock;

    public MaintenanceApplicationService(
        MaintenanceWorkRepository maintenanceWorkRepository,
        AssetServiceAssetLookup assetServiceAssetLookup,
        AuditWriter auditWriter,
        Clock clock
    ) {
        this.maintenanceWorkRepository = maintenanceWorkRepository;
        this.assetServiceAssetLookup = assetServiceAssetLookup;
        this.auditWriter = auditWriter;
        this.clock = clock;
    }

    public List<MaintenanceWork> findOpenWork(UUID tenantId) {
        return maintenanceWorkRepository.findOpen(tenantId);
    }

    public MaintenanceWork findWork(UUID tenantId, UUID workId) {
        return maintenanceWorkRepository.findById(tenantId, workId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance work not found"));
    }

    public AssetMaintenanceView findAssetView(UUID tenantId, UUID authenticatedUserId, UUID assetId) {
        requireTrackedAsset(tenantId, authenticatedUserId, assetId);
        MaintenanceWork openWork = maintenanceWorkRepository.findOpenByAssetId(tenantId, assetId).orElse(null);
        return toAssetView(assetId, openWork);
    }

    @Transactional
    public MaintenanceWork openWork(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID assetId,
        MaintenanceWorkType workType,
        AuditMetadata auditMetadata
    ) {
        requireTrackedAsset(tenantId, authenticatedUserId, assetId);
        if (maintenanceWorkRepository.findOpenByAssetId(tenantId, assetId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset already has current OPEN maintenance work");
        }

        Instant openedAt = Instant.now(clock);
        try {
            MaintenanceWork work = maintenanceWorkRepository.createOpen(tenantId, assetId, workType, openedAt);
            auditWriter.record(
                tenantId,
                auditMetadata,
                AuditEntity.MAINTENANCE_WORK,
                work.id(),
                AuditAction.OPEN_WORK,
                new WorkLifecycleAuditPayload(
                    work.assetId(),
                    work.workType(),
                    work.workState(),
                    work.openedAt(),
                    work.completedAt()
                )
            );
            return work;
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Asset already has current OPEN maintenance work",
                exception
            );
        }
    }

    @Transactional
    public MaintenanceWork completeWork(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID workId,
        AuditMetadata auditMetadata
    ) {
        MaintenanceWork existingWork = maintenanceWorkRepository.findById(tenantId, workId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance work not found"));
        requireTrackedAsset(tenantId, authenticatedUserId, existingWork.assetId());
        if (existingWork.workState() != MaintenanceWorkState.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Maintenance work is not OPEN");
        }

        MaintenanceWork completedWork = maintenanceWorkRepository.complete(tenantId, workId, Instant.now(clock))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Maintenance work is not OPEN"));
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.MAINTENANCE_WORK,
            completedWork.id(),
            AuditAction.COMPLETE_WORK,
            new WorkCompletionAuditPayload(
                completedWork.assetId(),
                completedWork.workType(),
                existingWork.workState(),
                completedWork.workState(),
                completedWork.completedAt()
            )
        );
        return completedWork;
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

    private AssetMaintenanceView toAssetView(UUID assetId, MaintenanceWork openWork) {
        if (openWork == null) {
            return new AssetMaintenanceView(assetId, false, true, null, null, null, null, null);
        }
        return new AssetMaintenanceView(
            assetId,
            true,
            false,
            openWork.id(),
            openWork.workType(),
            openWork.workState(),
            openWork.openedAt(),
            openWork.completedAt()
        );
    }

    public record AssetMaintenanceView(
        UUID assetId,
        boolean hasOpenWork,
        boolean serviceableNow,
        UUID workId,
        MaintenanceWorkType workType,
        MaintenanceWorkState workState,
        Instant openedAt,
        Instant completedAt
    ) {
    }

    private record WorkLifecycleAuditPayload(
        UUID assetId,
        MaintenanceWorkType workType,
        MaintenanceWorkState workState,
        Instant openedAt,
        Instant completedAt
    ) {
    }

    private record WorkCompletionAuditPayload(
        UUID assetId,
        MaintenanceWorkType workType,
        MaintenanceWorkState previousWorkState,
        MaintenanceWorkState newWorkState,
        Instant completedAt
    ) {
    }
}
