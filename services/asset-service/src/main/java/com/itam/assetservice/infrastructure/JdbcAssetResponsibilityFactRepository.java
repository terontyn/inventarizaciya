package com.itam.assetservice.infrastructure;

import com.itam.assetservice.domain.AssetResponsibilityFact;
import com.itam.assetservice.domain.AssetResponsibilityFactRepository;
import com.itam.assetservice.domain.AssetResponsibilityFactType;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcAssetResponsibilityFactRepository implements AssetResponsibilityFactRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcAssetResponsibilityFactRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void append(UUID tenantId, AssetResponsibilityFact fact) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("id", fact.id())
            .addValue("tenantId", scopedTenantId)
            .addValue("assetId", fact.assetId())
            .addValue("factType", fact.factType().name())
            .addValue("previousAssignedUserId", fact.previousAssignedUserId())
            .addValue("newAssignedUserId", fact.newAssignedUserId())
            .addValue("previousLocationId", fact.previousLocationId())
            .addValue("newLocationId", fact.newLocationId())
            .addValue("occurredAt", fact.occurredAt());
        jdbcTemplate.update(
            "INSERT INTO asset_responsibility_facts ("
                + "id, tenant_id, asset_id, fact_type, previous_assigned_user_id, new_assigned_user_id, "
                + "previous_location_id, new_location_id, occurred_at"
                + ") VALUES ("
                + ":id, :tenantId, :assetId, :factType, :previousAssignedUserId, :newAssignedUserId, "
                + ":previousLocationId, :newLocationId, :occurredAt"
                + ")",
            parameters
        );
    }

    @Override
    public List<AssetResponsibilityFact> findByAssetId(UUID tenantId, UUID assetId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, asset_id, fact_type, previous_assigned_user_id, new_assigned_user_id, "
                + "previous_location_id, new_location_id, occurred_at "
                + "FROM asset_responsibility_facts "
                + "WHERE tenant_id = :tenantId AND asset_id = :assetId "
                + "ORDER BY occurred_at DESC, id DESC",
            Map.of(
                "tenantId", scopedTenantId,
                "assetId", assetId
            ),
            (resultSet, rowNum) -> new AssetResponsibilityFact(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("tenant_id", UUID.class),
                resultSet.getObject("asset_id", UUID.class),
                AssetResponsibilityFactType.valueOf(resultSet.getString("fact_type")),
                resultSet.getObject("previous_assigned_user_id", UUID.class),
                resultSet.getObject("new_assigned_user_id", UUID.class),
                resultSet.getObject("previous_location_id", UUID.class),
                resultSet.getObject("new_location_id", UUID.class),
                resultSet.getTimestamp("occurred_at").toInstant()
            )
        );
    }
}
