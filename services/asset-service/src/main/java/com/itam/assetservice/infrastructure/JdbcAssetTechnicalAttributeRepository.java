package com.itam.assetservice.infrastructure;

import com.itam.assetservice.domain.AssetEmployeeSheetTechnicalHintRow;
import com.itam.assetservice.domain.AssetTechnicalAttributeKey;
import com.itam.assetservice.domain.AssetTechnicalAttributeRepository;
import com.itam.assetservice.domain.AssetTechnicalAttributeSearchResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcAssetTechnicalAttributeRepository implements AssetTechnicalAttributeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcAssetTechnicalAttributeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, String> findByAssetId(UUID tenantId, UUID assetId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT attribute_key, attribute_value "
                + "FROM asset_technical_attributes "
                + "WHERE tenant_id = :tenantId AND asset_id = :assetId "
                + "ORDER BY attribute_key",
            Map.of(
                "tenantId", scopedTenantId,
                "assetId", assetId
            ),
            resultSet -> {
                Map<String, String> values = new LinkedHashMap<>();
                while (resultSet.next()) {
                    values.put(
                        resultSet.getString("attribute_key"),
                        resultSet.getString("attribute_value")
                    );
                }
                return values;
            }
        );
    }

    @Override
    @Transactional
    public void replaceForAsset(UUID tenantId, UUID assetId, Map<String, String> values) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        Map<String, Object> deleteParameters = Map.of(
            "tenantId", scopedTenantId,
            "assetId", assetId
        );
        jdbcTemplate.update(
            "DELETE FROM asset_technical_attributes WHERE tenant_id = :tenantId AND asset_id = :assetId",
            deleteParameters
        );

        for (Map.Entry<String, String> entry : values.entrySet()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("tenantId", scopedTenantId)
                .addValue("assetId", assetId)
                .addValue("attributeKey", entry.getKey())
                .addValue("attributeValue", entry.getValue());
            jdbcTemplate.update(
                "INSERT INTO asset_technical_attributes ("
                    + "tenant_id, asset_id, attribute_key, attribute_value, created_at, updated_at"
                    + ") VALUES ("
                    + ":tenantId, :assetId, :attributeKey, :attributeValue, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP"
                    + ")",
                parameters
            );
        }
    }

    @Override
    public List<AssetTechnicalAttributeSearchResult> searchByTechnicalAttributes(
        UUID tenantId,
        String normalizedQuery,
        List<AssetTechnicalAttributeKey> searchableKeys,
        int limit
    ) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        List<String> keyNames = searchableKeys.stream()
            .map(AssetTechnicalAttributeKey::key)
            .toList();
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("tenantId", scopedTenantId)
            .addValue("keys", keyNames)
            .addValue("query", normalizedQuery)
            .addValue("prefixPattern", escapeLikePattern(normalizedQuery) + "%")
            .addValue("containsPattern", "%" + escapeLikePattern(normalizedQuery) + "%")
            .addValue("limit", limit);

        return jdbcTemplate.query(
            "SELECT asset_id, asset_tag, display_name, asset_type, serial_number, status, "
                + "attribute_key, attribute_value "
                + "FROM ("
                + "SELECT matched.*, "
                + "ROW_NUMBER() OVER ("
                + "PARTITION BY matched.asset_id "
                + "ORDER BY matched.match_rank, matched.key_priority, matched.asset_tag, matched.asset_id"
                + ") AS row_number "
                + "FROM ("
                + "SELECT a.id AS asset_id, a.asset_tag, a.display_name, a.asset_type, a.serial_number, a.status, "
                + "ata.attribute_key, ata.attribute_value, "
                + "CASE "
                + "WHEN LOWER(ata.attribute_value) = :query THEN 0 "
                + "WHEN LOWER(ata.attribute_value) LIKE :prefixPattern ESCAPE '\\' THEN 1 "
                + "ELSE 2 "
                + "END AS match_rank, "
                + keyPriorityCaseSql()
                + " AS key_priority "
                + "FROM asset_technical_attributes ata "
                + "JOIN assets a ON ata.tenant_id = a.tenant_id AND ata.asset_id = a.id "
                + "WHERE ata.tenant_id = :tenantId "
                + "AND ata.attribute_key IN (:keys) "
                + "AND LOWER(ata.attribute_value) LIKE :containsPattern ESCAPE '\\'"
                + ") matched"
                + ") ranked "
                + "WHERE ranked.row_number = 1 "
                + "ORDER BY ranked.match_rank, ranked.key_priority, ranked.asset_tag, ranked.asset_id "
                + "LIMIT :limit",
            parameters,
            (resultSet, rowNum) -> new AssetTechnicalAttributeSearchResult(
                resultSet.getObject("asset_id", UUID.class),
                resultSet.getString("asset_tag"),
                resultSet.getString("display_name"),
                resultSet.getString("asset_type"),
                resultSet.getString("serial_number"),
                resultSet.getString("status"),
                AssetTechnicalAttributeKey.fromKey(resultSet.getString("attribute_key")).orElseThrow(),
                resultSet.getString("attribute_value")
            )
        );
    }

    @Override
    public List<AssetEmployeeSheetTechnicalHintRow> findEmployeeSheetTechnicalHints(
        UUID tenantId,
        UUID assignedUserId,
        List<AssetTechnicalAttributeKey> allowedHintKeys
    ) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        List<String> keyNames = allowedHintKeys.stream()
            .map(AssetTechnicalAttributeKey::key)
            .toList();
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("tenantId", scopedTenantId)
            .addValue("assignedUserId", assignedUserId)
            .addValue("keys", keyNames);

        return jdbcTemplate.query(
            "SELECT a.id AS asset_id, a.asset_tag, a.display_name, a.asset_type, "
                + "a.serial_number, a.status, a.location_id, "
                + "ata.attribute_key, ata.attribute_value "
                + "FROM assets a "
                + "LEFT JOIN asset_technical_attributes ata "
                + "ON ata.tenant_id = a.tenant_id "
                + "AND ata.asset_id = a.id "
                + "AND ata.attribute_key IN (:keys) "
                + "WHERE a.tenant_id = :tenantId "
                + "AND a.assigned_user_id = :assignedUserId "
                + "ORDER BY a.asset_tag, a.id, "
                + "CASE ata.attribute_key "
                + "WHEN 'hostname' THEN 1 "
                + "WHEN 'extension_number' THEN 2 "
                + "WHEN 'printer_network_name' THEN 3 "
                + "ELSE 99 "
                + "END",
            parameters,
            (resultSet, rowNum) -> {
                String rawKey = resultSet.getString("attribute_key");
                return new AssetEmployeeSheetTechnicalHintRow(
                    resultSet.getObject("asset_id", UUID.class),
                    resultSet.getString("asset_tag"),
                    resultSet.getString("display_name"),
                    resultSet.getString("asset_type"),
                    resultSet.getString("serial_number"),
                    resultSet.getString("status"),
                    resultSet.getObject("location_id", UUID.class),
                    rawKey == null ? null : AssetTechnicalAttributeKey.fromKey(rawKey).orElseThrow(),
                    resultSet.getString("attribute_value")
                );
            }
        );
    }

    private static String keyPriorityCaseSql() {
        return "CASE ata.attribute_key "
            + "WHEN 'hostname' THEN 1 "
            + "WHEN 'anydesk_id' THEN 2 "
            + "WHEN 'ip_address' THEN 3 "
            + "WHEN 'mac_address' THEN 4 "
            + "WHEN 'phone_ip' THEN 5 "
            + "WHEN 'extension_number' THEN 6 "
            + "WHEN 'sip_line_id' THEN 7 "
            + "WHEN 'printer_ip' THEN 8 "
            + "WHEN 'printer_network_name' THEN 9 "
            + "WHEN 'printer_placement_note' THEN 10 "
            + "ELSE 99 "
            + "END";
    }

    private static String escapeLikePattern(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
    }
}
