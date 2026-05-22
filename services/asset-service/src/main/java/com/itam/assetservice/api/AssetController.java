package com.itam.assetservice.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.itam.assetservice.application.AssetApplicationService;
import com.itam.assetservice.application.AssetAuthorizationService;
import com.itam.assetservice.application.AssetReviewReadService;
import com.itam.assetservice.application.AssetResponsibilityApplicationService;
import com.itam.assetservice.application.AssetTechnicalAttributeApplicationService;
import com.itam.assetservice.domain.Asset;
import com.itam.assetservice.domain.AssetResponsibilityFact;
import com.itam.assetservice.domain.AssetTechnicalAttributes;
import com.itam.assetservice.domain.AuditRecord;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Validated
@RequestMapping("/api/v1/assets")
public class AssetController {

    private final AssetApplicationService assetApplicationService;
    private final AssetAuthorizationService assetAuthorizationService;
    private final AssetReviewReadService assetReviewReadService;
    private final AssetResponsibilityApplicationService assetResponsibilityApplicationService;
    private final AssetTechnicalAttributeApplicationService assetTechnicalAttributeApplicationService;
    private final AuthenticatedRequestContextExtractor requestContextExtractor;

    public AssetController(
        AssetApplicationService assetApplicationService,
        AssetAuthorizationService assetAuthorizationService,
        AssetReviewReadService assetReviewReadService,
        AssetResponsibilityApplicationService assetResponsibilityApplicationService,
        AssetTechnicalAttributeApplicationService assetTechnicalAttributeApplicationService,
        AuthenticatedRequestContextExtractor requestContextExtractor
    ) {
        this.assetApplicationService = assetApplicationService;
        this.assetAuthorizationService = assetAuthorizationService;
        this.assetReviewReadService = assetReviewReadService;
        this.assetResponsibilityApplicationService = assetResponsibilityApplicationService;
        this.assetTechnicalAttributeApplicationService = assetTechnicalAttributeApplicationService;
        this.requestContextExtractor = requestContextExtractor;
    }

    @PostMapping
    public ResponseEntity<AssetResponse> create(
        @Valid @RequestBody AssetCreateRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        assetAuthorizationService.requireAssetMutateAccess(tenantId, authenticatedUserId);
        Asset asset = assetApplicationService.create(
            tenantId,
            authenticatedUserId,
            request.assetTag(),
            request.serialNumber(),
            request.assetType(),
            request.assignedUserId(),
            request.locationId(),
            request.displayName(),
            request.note(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return ResponseEntity
            .created(URI.create("/api/v1/assets/" + asset.id()))
            .body(AssetResponse.from(asset));
    }

    @GetMapping
    public List<AssetResponse> list(HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        assetAuthorizationService.requireAssetReadAccess(tenantId, authenticatedUserId);
        return assetApplicationService.findAll(tenantId).stream()
            .map(AssetResponse::from)
            .toList();
    }

    @GetMapping("/technical-attribute-search")
    public TechnicalAttributeSearchResponse searchTechnicalAttributes(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) Integer limit,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        assetAuthorizationService.requireAssetReadAccess(tenantId, authenticatedUserId);
        rejectUnsupportedTechnicalAttributeSearchParameters(servletRequest);
        return TechnicalAttributeSearchResponse.from(
            assetTechnicalAttributeApplicationService.search(tenantId, q, limit)
        );
    }

    @GetMapping("/{id}")
    public AssetDetailResponse get(@PathVariable UUID id, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        assetAuthorizationService.requireAssetReadAccess(tenantId, authenticatedUserId);
        Asset asset = assetApplicationService.findById(tenantId, id);
        return AssetDetailResponse.from(
            asset,
            assetTechnicalAttributeApplicationService.findForAsset(asset)
        );
    }

    @GetMapping("/{id}/history")
    public AssetHistoryResponse history(@PathVariable UUID id, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        assetAuthorizationService.requireAssetReadAccess(tenantId, authenticatedUserId);
        return new AssetHistoryResponse(
            id,
            assetReviewReadService.findHistory(tenantId, id).stream()
                .map(AssetHistoryItemResponse::from)
                .toList()
        );
    }

    @GetMapping("/{id}/audit")
    public AssetAuditResponse audit(@PathVariable UUID id, HttpServletRequest servletRequest) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        assetAuthorizationService.requireAssetReadAccess(tenantId, authenticatedUserId);
        return new AssetAuditResponse(
            id,
            assetReviewReadService.findAudit(tenantId, id).stream()
                .map(AssetAuditItemResponse::from)
                .toList()
        );
    }

    @PatchMapping("/{id}")
    public AssetResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody AssetPatchRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        assetAuthorizationService.requireAssetMutateAccess(tenantId, authenticatedUserId);
        return AssetResponse.from(assetApplicationService.update(
            tenantId,
            id,
            request.assetTag(),
            request.serialNumber(),
            request.assetType(),
            request.displayNameValue(),
            request.touchesDisplayName(),
            request.noteValue(),
            request.touchesNote(),
            request.touchesAssignedUserId(),
            request.touchesLocationId(),
            request.touchesTechnicalAttributes(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        ));
    }

    @PutMapping("/{id}/technical-attributes")
    public TechnicalAttributesUpdateResponse updateTechnicalAttributes(
        @PathVariable UUID id,
        @RequestBody JsonNode request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        assetAuthorizationService.requireAssetMutateAccess(tenantId, authenticatedUserId);
        return TechnicalAttributesUpdateResponse.from(assetTechnicalAttributeApplicationService.replace(
            tenantId,
            id,
            technicalAttributeValues(request),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        ));
    }

    @PostMapping("/{id}/assign")
    public AssignAssetResponse assign(
        @PathVariable UUID id,
        @Valid @RequestBody AssignAssetRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        assetAuthorizationService.requireAssetMutateAccess(tenantId, authenticatedUserId);
        AssetResponsibilityApplicationService.AssignResult result = assetResponsibilityApplicationService.assign(
            tenantId,
            authenticatedUserId,
            id,
            request.targetUserId(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return new AssignAssetResponse(result.id(), result.assignedUserId());
    }

    @PostMapping("/{id}/transfer")
    public TransferAssetResponse transfer(
        @PathVariable UUID id,
        @Valid @RequestBody TransferAssetRequest request,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        assetAuthorizationService.requireAssetMutateAccess(tenantId, authenticatedUserId);
        AssetResponsibilityApplicationService.TransferResult result = assetResponsibilityApplicationService.transfer(
            tenantId,
            authenticatedUserId,
            id,
            request.targetUserId(),
            request.targetLocationId(),
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return new TransferAssetResponse(result.id(), result.assignedUserId(), result.locationId());
    }

    @PostMapping("/{id}/return")
    public ReturnAssetResponse returnAsset(
        @PathVariable UUID id,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        assetAuthorizationService.requireAssetMutateAccess(tenantId, authenticatedUserId);
        AssetResponsibilityApplicationService.ReturnResult result = assetResponsibilityApplicationService.returnAsset(
            tenantId,
            id,
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return new ReturnAssetResponse(result.id());
    }

    @PostMapping("/{id}/retire")
    public RetireAssetResponse retire(
        @PathVariable UUID id,
        HttpServletRequest servletRequest
    ) {
        UUID tenantId = requestContextExtractor.requireTenantId(servletRequest);
        UUID authenticatedUserId = requestContextExtractor.requireAuthenticatedUserId(servletRequest);
        assetAuthorizationService.requireRetireForbiddenByPilotCutline(tenantId, authenticatedUserId);
        AssetResponsibilityApplicationService.RetireResult result = assetResponsibilityApplicationService.retire(
            tenantId,
            id,
            requestContextExtractor.requireAuditMetadata(servletRequest)
        );
        return new RetireAssetResponse(result.id());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AssetCreateRequest(
        @NotBlank String assetTag,
        @NotBlank String serialNumber,
        @NotBlank String assetType,
        UUID assignedUserId,
        UUID locationId,
        String displayName,
        String note
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AssetPatchRequest(
        @NotBlank String assetTag,
        @NotBlank String serialNumber,
        @NotBlank String assetType,
        JsonNode displayName,
        JsonNode note,
        JsonNode assignedUserId,
        JsonNode locationId,
        JsonNode technicalAttributes
    ) {
        public boolean touchesDisplayName() {
            return displayName != null;
        }

        public boolean touchesNote() {
            return note != null;
        }

        public boolean touchesAssignedUserId() {
            return assignedUserId != null;
        }

        public boolean touchesLocationId() {
            return locationId != null;
        }

        public boolean touchesTechnicalAttributes() {
            return technicalAttributes != null;
        }

        public String displayNameValue() {
            return optionalTextValue(displayName, "display_name");
        }

        public String noteValue() {
            return optionalTextValue(note, "note");
        }

        private static String optionalTextValue(JsonNode node, String fieldName) {
            if (node == null || node.isNull()) {
                return null;
            }
            if (!node.isTextual()) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must be a string or null"
                );
            }
            return node.asText();
        }
    }

    public record AssetResponse(
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

        private static AssetResponse from(Asset asset) {
            return new AssetResponse(
                asset.id(),
                asset.assetTag(),
                asset.serialNumber(),
                asset.assetType(),
                asset.status(),
                asset.assignedUserId(),
                asset.locationId(),
                asset.displayName(),
                asset.note()
            );
        }
    }

    public record AssetDetailResponse(
        UUID id,
        String assetTag,
        String serialNumber,
        String assetType,
        String status,
        UUID assignedUserId,
        UUID locationId,
        String displayName,
        String note,
        TechnicalAttributesResponse technicalAttributes
    ) {

        private static AssetDetailResponse from(
            Asset asset,
            AssetTechnicalAttributes technicalAttributes
        ) {
            return new AssetDetailResponse(
                asset.id(),
                asset.assetTag(),
                asset.serialNumber(),
                asset.assetType(),
                asset.status(),
                asset.assignedUserId(),
                asset.locationId(),
                asset.displayName(),
                asset.note(),
                TechnicalAttributesResponse.from(technicalAttributes)
            );
        }
    }

    public record TechnicalAttributesResponse(
        String profile,
        Map<String, String> values
    ) {

        private static TechnicalAttributesResponse from(AssetTechnicalAttributes technicalAttributes) {
            return new TechnicalAttributesResponse(
                technicalAttributes.profile().name(),
                technicalAttributes.values()
            );
        }
    }

    public record TechnicalAttributesUpdateResponse(
        UUID assetId,
        String assetTag,
        String profile,
        Map<String, String> values
    ) {

        private static TechnicalAttributesUpdateResponse from(
            AssetTechnicalAttributeApplicationService.UpdateResult result
        ) {
            return new TechnicalAttributesUpdateResponse(
                result.assetId(),
                result.assetTag(),
                result.profile().name(),
                result.values()
            );
        }
    }

    public record TechnicalAttributeSearchResponse(
        String query,
        int limit,
        List<TechnicalAttributeSearchAssetResponse> results
    ) {

        private static TechnicalAttributeSearchResponse from(
            AssetTechnicalAttributeApplicationService.SearchResponse result
        ) {
            return new TechnicalAttributeSearchResponse(
                result.query(),
                result.limit(),
                result.results().stream()
                    .map(TechnicalAttributeSearchAssetResponse::from)
                    .toList()
            );
        }
    }

    public record TechnicalAttributeSearchAssetResponse(
        UUID assetId,
        String assetTag,
        String displayName,
        String assetType,
        String serialNumber,
        String status,
        TechnicalAttributeMatchedFieldResponse matchedField
    ) {

        private static TechnicalAttributeSearchAssetResponse from(
            AssetTechnicalAttributeApplicationService.SearchAssetResult result
        ) {
            return new TechnicalAttributeSearchAssetResponse(
                result.assetId(),
                result.assetTag(),
                result.displayName(),
                result.assetType(),
                result.serialNumber(),
                result.status(),
                TechnicalAttributeMatchedFieldResponse.from(result.matchedField())
            );
        }
    }

    public record TechnicalAttributeMatchedFieldResponse(
        String key,
        String label,
        String displayValue,
        boolean sensitive,
        boolean masked
    ) {

        private static TechnicalAttributeMatchedFieldResponse from(
            AssetTechnicalAttributeApplicationService.MatchedField matchedField
        ) {
            return new TechnicalAttributeMatchedFieldResponse(
                matchedField.key(),
                matchedField.label(),
                matchedField.displayValue(),
                matchedField.sensitive(),
                matchedField.masked()
            );
        }
    }

    public record AssignAssetRequest(@NotNull UUID targetUserId) {
    }

    public record TransferAssetRequest(UUID targetUserId, UUID targetLocationId) {
    }

    public record AssignAssetResponse(UUID id, UUID assignedUserId) {
    }

    public record TransferAssetResponse(UUID id, UUID assignedUserId, UUID locationId) {
    }

    public record ReturnAssetResponse(UUID id) {
    }

    public record RetireAssetResponse(UUID id) {
    }

    public record AssetHistoryResponse(UUID assetId, List<AssetHistoryItemResponse> items) {
    }

    public record AssetHistoryItemResponse(
        String factType,
        Instant occurredAt,
        UUID previousAssignedUserId,
        UUID newAssignedUserId,
        UUID previousLocationId,
        UUID newLocationId
    ) {

        private static AssetHistoryItemResponse from(AssetResponsibilityFact fact) {
            return new AssetHistoryItemResponse(
                fact.factType().name(),
                fact.occurredAt(),
                fact.previousAssignedUserId(),
                fact.newAssignedUserId(),
                fact.previousLocationId(),
                fact.newLocationId()
            );
        }
    }

    public record AssetAuditResponse(UUID assetId, List<AssetAuditItemResponse> items) {
    }

    public record AssetAuditItemResponse(
        String action,
        String entity,
        UUID entityId,
        String actor,
        String correlationId,
        Instant occurredAt,
        String payload
    ) {

        private static AssetAuditItemResponse from(AuditRecord auditRecord) {
            return new AssetAuditItemResponse(
                auditRecord.actionType().name(),
                auditRecord.entityType().name(),
                auditRecord.entityId(),
                auditRecord.actor(),
                auditRecord.correlationId(),
                auditRecord.occurredAt(),
                auditRecord.changePayload()
            );
        }
    }

    private static Map<String, String> technicalAttributeValues(JsonNode root) {
        if (root == null || !root.isObject()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Technical attributes request must be an object"
            );
        }

        Iterator<Map.Entry<String, JsonNode>> rootFields = root.fields();
        while (rootFields.hasNext()) {
            Map.Entry<String, JsonNode> field = rootFields.next();
            if (!"values".equals(field.getKey())) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Technical attributes request supports only values"
                );
            }
        }

        JsonNode valuesNode = root.get("values");
        if (valuesNode == null || !valuesNode.isObject()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "values must be an object"
            );
        }

        Map<String, String> values = new LinkedHashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = valuesNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode value = field.getValue();
            if (value == null || value.isNull()) {
                values.put(field.getKey(), null);
            } else if (value.isTextual()) {
                values.put(field.getKey(), value.asText());
            } else {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "technical attribute values must be strings or null"
                );
            }
        }
        return values;
    }

    private static void rejectUnsupportedTechnicalAttributeSearchParameters(HttpServletRequest request) {
        Set<String> supportedParameters = Set.of("q", "limit");
        for (String parameterName : request.getParameterMap().keySet()) {
            if (!supportedParameters.contains(parameterName)) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "technical attribute search supports only q and limit"
                );
            }
        }
    }
}
