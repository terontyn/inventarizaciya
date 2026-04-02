package com.itam.assetservice.application;

import com.itam.assetservice.domain.AssetRepository;
import com.itam.assetservice.domain.AssetResponsibilityFact;
import com.itam.assetservice.domain.AssetResponsibilityFactRepository;
import com.itam.assetservice.domain.AuditEntity;
import com.itam.assetservice.domain.AuditRecord;
import com.itam.assetservice.domain.AuditRecordRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssetReviewReadService {

    private final AssetRepository assetRepository;
    private final AssetResponsibilityFactRepository assetResponsibilityFactRepository;
    private final AuditRecordRepository auditRecordRepository;

    public AssetReviewReadService(
        AssetRepository assetRepository,
        AssetResponsibilityFactRepository assetResponsibilityFactRepository,
        AuditRecordRepository auditRecordRepository
    ) {
        this.assetRepository = assetRepository;
        this.assetResponsibilityFactRepository = assetResponsibilityFactRepository;
        this.auditRecordRepository = auditRecordRepository;
    }

    public List<AssetResponsibilityFact> findHistory(UUID tenantId, UUID assetId) {
        requireAsset(tenantId, assetId);
        return assetResponsibilityFactRepository.findByAssetId(tenantId, assetId);
    }

    public List<AuditRecord> findAudit(UUID tenantId, UUID assetId) {
        requireAsset(tenantId, assetId);
        return auditRecordRepository.findByEntity(tenantId, AuditEntity.ASSET, assetId);
    }

    private void requireAsset(UUID tenantId, UUID assetId) {
        assetRepository.findById(tenantId, assetId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));
    }
}
