package com.itam.assetservice.application;

import com.itam.assetservice.domain.Asset;
import com.itam.assetservice.domain.AssetRepository;
import com.itam.assetservice.domain.AssetTechnicalAttributeKey;
import com.itam.assetservice.domain.AssetTechnicalAttributeProfile;
import com.itam.assetservice.domain.AssetTechnicalAttributeProfileResolver;
import com.itam.assetservice.domain.AssetTechnicalAttributeRepository;
import com.itam.assetservice.domain.AssetTechnicalAttributes;
import com.itam.assetservice.domain.AuditAction;
import com.itam.assetservice.domain.AuditEntity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssetTechnicalAttributeApplicationService {

    private static final int ATTRIBUTE_VALUE_MAX_LENGTH = 512;
    private static final String SENSITIVITY_NOTE =
        "Technical attributes may contain operational network or remote access data.";

    private final AssetRepository assetRepository;
    private final AssetTechnicalAttributeRepository technicalAttributeRepository;
    private final AssetTechnicalAttributeProfileResolver profileResolver;
    private final AuditWriter auditWriter;

    public AssetTechnicalAttributeApplicationService(
        AssetRepository assetRepository,
        AssetTechnicalAttributeRepository technicalAttributeRepository,
        AssetTechnicalAttributeProfileResolver profileResolver,
        AuditWriter auditWriter
    ) {
        this.assetRepository = assetRepository;
        this.technicalAttributeRepository = technicalAttributeRepository;
        this.profileResolver = profileResolver;
        this.auditWriter = auditWriter;
    }

    public AssetTechnicalAttributes findForAsset(Asset asset) {
        AssetTechnicalAttributeProfile profile = profileResolver.resolve(asset.assetType());
        Map<String, String> storedValues = technicalAttributeRepository.findByAssetId(
            asset.tenantId(),
            asset.id()
        );
        return new AssetTechnicalAttributes(
            asset.id(),
            profile,
            visibleValues(profile, storedValues)
        );
    }

    @Transactional
    public UpdateResult replace(
        UUID tenantId,
        UUID assetId,
        Map<String, String> requestedValues,
        AuditMetadata auditMetadata
    ) {
        Asset asset = requireAsset(tenantId, assetId);
        AssetTechnicalAttributeProfile profile = profileResolver.resolve(asset.assetType());
        if (profile == AssetTechnicalAttributeProfile.UNSUPPORTED_GENERIC) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Technical attributes are not configured for this asset_type"
            );
        }

        Map<String, String> normalizedValues = normalizeRequestedValues(profile, requestedValues);
        Map<String, String> beforeValues = visibleValues(
            profile,
            technicalAttributeRepository.findByAssetId(tenantId, assetId)
        );
        if (beforeValues.equals(normalizedValues)) {
            return new UpdateResult(asset.id(), asset.assetTag(), profile, beforeValues);
        }

        technicalAttributeRepository.replaceForAsset(tenantId, assetId, normalizedValues);
        Map<String, String> savedValues = visibleValues(
            profile,
            technicalAttributeRepository.findByAssetId(tenantId, assetId)
        );
        ChangedValues changedValues = changedValues(profile, beforeValues, savedValues);
        auditWriter.record(
            tenantId,
            auditMetadata,
            AuditEntity.ASSET,
            asset.id(),
            AuditAction.ASSET_TECHNICAL_ATTRIBUTES_UPDATED,
            new TechnicalAttributeAuditPayload(
                asset.id(),
                asset.assetTag(),
                profile.name(),
                changedValues.changedKeys(),
                changedValues.before(),
                changedValues.after(),
                SENSITIVITY_NOTE
            )
        );

        return new UpdateResult(asset.id(), asset.assetTag(), profile, savedValues);
    }

    private Asset requireAsset(UUID tenantId, UUID assetId) {
        return assetRepository.findById(tenantId, assetId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));
    }

    private Map<String, String> normalizeRequestedValues(
        AssetTechnicalAttributeProfile profile,
        Map<String, String> requestedValues
    ) {
        Map<String, String> normalizedValues = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : requestedValues.entrySet()) {
            AssetTechnicalAttributeKey key = AssetTechnicalAttributeKey.fromKey(entry.getKey())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unknown technical attribute key: " + entry.getKey()
                ));
            if (!profile.allows(key)) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Technical attribute key is not allowed for this asset_type: " + key.key()
                );
            }
            if (normalizedValues.containsKey(key.key())) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Duplicate technical attribute key: " + key.key()
                );
            }

            String normalizedValue = InputNormalizer.optionalTrimmed(
                entry.getValue(),
                key.key(),
                ATTRIBUTE_VALUE_MAX_LENGTH,
                true
            );
            if (normalizedValue != null) {
                normalizedValues.put(key.key(), normalizedValue);
            }
        }
        return orderedValues(profile, normalizedValues);
    }

    private Map<String, String> visibleValues(
        AssetTechnicalAttributeProfile profile,
        Map<String, String> storedValues
    ) {
        if (profile == AssetTechnicalAttributeProfile.UNSUPPORTED_GENERIC) {
            return Map.of();
        }
        return orderedValues(profile, storedValues);
    }

    private Map<String, String> orderedValues(
        AssetTechnicalAttributeProfile profile,
        Map<String, String> values
    ) {
        Map<String, String> orderedValues = new LinkedHashMap<>();
        for (AssetTechnicalAttributeKey key : profile.allowedKeys()) {
            String value = values.get(key.key());
            if (value != null) {
                orderedValues.put(key.key(), value);
            }
        }
        return orderedValues;
    }

    private ChangedValues changedValues(
        AssetTechnicalAttributeProfile profile,
        Map<String, String> beforeValues,
        Map<String, String> afterValues
    ) {
        List<String> changedKeys = new ArrayList<>();
        Map<String, String> before = new LinkedHashMap<>();
        Map<String, String> after = new LinkedHashMap<>();
        for (AssetTechnicalAttributeKey key : profile.allowedKeys()) {
            String keyName = key.key();
            String beforeValue = beforeValues.get(keyName);
            String afterValue = afterValues.get(keyName);
            if (!Objects.equals(beforeValue, afterValue)) {
                changedKeys.add(keyName);
                before.put(keyName, beforeValue);
                after.put(keyName, afterValue);
            }
        }
        return new ChangedValues(changedKeys, before, after);
    }

    public record UpdateResult(
        UUID assetId,
        String assetTag,
        AssetTechnicalAttributeProfile profile,
        Map<String, String> values
    ) {
    }

    private record ChangedValues(
        List<String> changedKeys,
        Map<String, String> before,
        Map<String, String> after
    ) {
    }

    private record TechnicalAttributeAuditPayload(
        UUID assetId,
        String assetTag,
        String profile,
        List<String> changedKeys,
        Map<String, String> before,
        Map<String, String> after,
        String sensitivityNote
    ) {
    }
}
