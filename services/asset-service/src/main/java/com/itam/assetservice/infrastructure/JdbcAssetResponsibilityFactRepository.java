package com.itam.assetservice.infrastructure;

import com.itam.assetservice.domain.AssetResponsibilityFact;
import com.itam.assetservice.domain.AssetResponsibilityFactRepository;
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
}
