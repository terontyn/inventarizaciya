package com.itam.assetservice.application;

import com.itam.assetservice.domain.Asset;
import com.itam.assetservice.domain.AssetRepository;
import com.itam.assetservice.domain.AssetTechnicalAttributeKey;
import com.itam.assetservice.domain.AssetTechnicalAttributeProfile;
import com.itam.assetservice.domain.AssetTechnicalAttributeProfileResolver;
import com.itam.assetservice.domain.AssetTechnicalAttributeRepository;
import com.itam.assetservice.domain.AssetTechnicalAttributeSearchResult;
import com.itam.assetservice.domain.AssetTechnicalAttributes;
import com.itam.assetservice.domain.AuditAction;
import com.itam.assetservice.domain.AuditEntity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssetTechnicalAttributeApplicationService {

    private static final int ATTRIBUTE_VALUE_MAX_LENGTH = 512;
    private static final int SEARCH_QUERY_MIN_LENGTH = 2;
    private static final int SEARCH_QUERY_MAX_LENGTH = 128;
    private static final int DEFAULT_SEARCH_LIMIT = 10;
    private static final int MAX_SEARCH_LIMIT = 20;
    private static final String SENSITIVITY_NOTE =
        "Technical attributes may contain operational network or remote access data.";
    private static final List<AssetTechnicalAttributeKey> SEARCHABLE_KEYS = List.of(
        AssetTechnicalAttributeKey.HOSTNAME,
        AssetTechnicalAttributeKey.ANYDESK_ID,
        AssetTechnicalAttributeKey.IP_ADDRESS,
        AssetTechnicalAttributeKey.MAC_ADDRESS,
        AssetTechnicalAttributeKey.PHONE_IP,
        AssetTechnicalAttributeKey.EXTENSION_NUMBER,
        AssetTechnicalAttributeKey.SIP_LINE_ID,
        AssetTechnicalAttributeKey.PRINTER_IP,
        AssetTechnicalAttributeKey.PRINTER_NETWORK_NAME
    );
    private static final Set<AssetTechnicalAttributeKey> SENSITIVE_SEARCH_KEYS = Set.of(
        AssetTechnicalAttributeKey.ANYDESK_ID,
        AssetTechnicalAttributeKey.IP_ADDRESS,
        AssetTechnicalAttributeKey.MAC_ADDRESS,
        AssetTechnicalAttributeKey.PHONE_IP,
        AssetTechnicalAttributeKey.SIP_LINE_ID,
        AssetTechnicalAttributeKey.PRINTER_IP
    );

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

    public SearchResponse search(UUID tenantId, String query, Integer requestedLimit) {
        String normalizedQuery = normalizeSearchQuery(query);
        int limit = normalizeSearchLimit(requestedLimit);
        List<SearchAssetResult> results = technicalAttributeRepository.searchByTechnicalAttributes(
                tenantId,
                normalizedQuery.toLowerCase(Locale.ROOT),
                SEARCHABLE_KEYS,
                limit
            ).stream()
            .map(this::toSearchAssetResult)
            .toList();
        return new SearchResponse(normalizedQuery, limit, results);
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

    private SearchAssetResult toSearchAssetResult(AssetTechnicalAttributeSearchResult result) {
        return new SearchAssetResult(
            result.assetId(),
            result.assetTag(),
            result.displayName(),
            result.assetType(),
            result.serialNumber(),
            result.status(),
            matchedField(result.matchedKey(), result.matchedValue())
        );
    }

    private MatchedField matchedField(AssetTechnicalAttributeKey key, String value) {
        boolean sensitive = SENSITIVE_SEARCH_KEYS.contains(key);
        return new MatchedField(
            key.key(),
            labelFor(key),
            sensitive ? maskSensitiveValue(key, value) : value,
            sensitive,
            sensitive
        );
    }

    private static String normalizeSearchQuery(String query) {
        if (query == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "q is required");
        }
        String normalizedQuery = query.trim();
        if (normalizedQuery.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "q must not be blank");
        }
        if (normalizedQuery.length() < SEARCH_QUERY_MIN_LENGTH) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "q must be at least " + SEARCH_QUERY_MIN_LENGTH + " characters"
            );
        }
        if (normalizedQuery.length() > SEARCH_QUERY_MAX_LENGTH) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "q must not exceed " + SEARCH_QUERY_MAX_LENGTH + " characters"
            );
        }
        return normalizedQuery;
    }

    private static int normalizeSearchLimit(Integer requestedLimit) {
        if (requestedLimit == null) {
            return DEFAULT_SEARCH_LIMIT;
        }
        if (requestedLimit < 1 || requestedLimit > MAX_SEARCH_LIMIT) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "limit must be between 1 and " + MAX_SEARCH_LIMIT
            );
        }
        return requestedLimit;
    }

    private static String labelFor(AssetTechnicalAttributeKey key) {
        return switch (key) {
            case HOSTNAME -> "Hostname";
            case ANYDESK_ID -> "AnyDesk ID";
            case IP_ADDRESS -> "IP-адрес";
            case MAC_ADDRESS -> "MAC-адрес";
            case PHONE_IP -> "IP телефона";
            case EXTENSION_NUMBER -> "Внутренний номер";
            case SIP_LINE_ID -> "SIP / линия";
            case PRINTER_IP -> "IP принтера";
            case PRINTER_NETWORK_NAME -> "Сетевое имя принтера";
            default -> throw new IllegalArgumentException("Unsupported technical attribute search key");
        };
    }

    private static String maskSensitiveValue(AssetTechnicalAttributeKey key, String value) {
        if (value == null || value.isBlank()) {
            return "\u2022\u2022\u2022\u2022";
        }
        String trimmedValue = value.trim();
        return switch (key) {
            case ANYDESK_ID -> maskTrailingValue(trimmedValue);
            case IP_ADDRESS, PHONE_IP, PRINTER_IP -> maskIpLikeValue(trimmedValue);
            case MAC_ADDRESS -> maskMacLikeValue(trimmedValue);
            case SIP_LINE_ID -> maskMiddleValue(trimmedValue);
            default -> maskMiddleValue(trimmedValue);
        };
    }

    private static String maskTrailingValue(String value) {
        if (value.length() < 4) {
            return "\u2022\u2022\u2022\u2022";
        }
        return "\u2022\u2022\u2022\u2022" + value.substring(value.length() - 4);
    }

    private static String maskIpLikeValue(String value) {
        String[] parts = value.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + "." + parts[2] + ".xxx";
        }
        return maskMiddleValue(value);
    }

    private static String maskMacLikeValue(String value) {
        String normalizedValue = value.replace("-", ":");
        String[] parts = normalizedValue.split(":");
        if (parts.length == 6) {
            return parts[0] + ":**:**:**:**:" + parts[5];
        }
        return maskMiddleValue(value);
    }

    private static String maskMiddleValue(String value) {
        if (value.length() < 5) {
            return "\u2022\u2022\u2022\u2022";
        }
        return value.substring(0, Math.min(3, value.length() - 2))
            + "\u2026"
            + value.substring(value.length() - 2);
    }

    public record UpdateResult(
        UUID assetId,
        String assetTag,
        AssetTechnicalAttributeProfile profile,
        Map<String, String> values
    ) {
    }

    public record SearchResponse(
        String query,
        int limit,
        List<SearchAssetResult> results
    ) {
    }

    public record SearchAssetResult(
        UUID assetId,
        String assetTag,
        String displayName,
        String assetType,
        String serialNumber,
        String status,
        MatchedField matchedField
    ) {
    }

    public record MatchedField(
        String key,
        String label,
        String displayValue,
        boolean sensitive,
        boolean masked
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
