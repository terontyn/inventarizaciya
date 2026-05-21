package com.itam.assetservice.infrastructure;

import com.itam.assetservice.domain.AssetTechnicalAttributeRepository;
import java.util.LinkedHashMap;
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
}
