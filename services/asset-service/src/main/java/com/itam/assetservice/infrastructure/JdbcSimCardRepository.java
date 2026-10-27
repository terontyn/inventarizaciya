package com.itam.assetservice.infrastructure;

import com.itam.assetservice.domain.SimCard;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSimCardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<SimCard> ROW_MAPPER = (rs, rowNum) -> new SimCard(
        rs.getObject("id", UUID.class),
        rs.getObject("tenant_id", UUID.class),
        rs.getString("phone_number"),
        rs.getString("operator"),
        rs.getString("tariff"),
        rs.getString("issued_to_name"),
        rs.getString("location_name"),
        rs.getString("status")
    );

    public JdbcSimCardRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SimCard> findAll(UUID tenantId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, phone_number, operator, tariff, issued_to_name, location_name, status FROM sim_cards WHERE tenant_id = :tenantId ORDER BY phone_number",
            Map.of("tenantId", scopedTenantId),
            ROW_MAPPER
        );
    }

    public Optional<SimCard> findById(UUID tenantId, UUID id) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        List<SimCard> results = jdbcTemplate.query(
            "SELECT id, tenant_id, phone_number, operator, tariff, issued_to_name, location_name, status FROM sim_cards WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id),
            ROW_MAPPER
        );
        return results.stream().findFirst();
    }

    public SimCard save(SimCard simCard) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(simCard.tenantId());
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", simCard.id())
            .addValue("tenantId", scopedTenantId)
            .addValue("phoneNumber", simCard.phoneNumber())
            .addValue("operator", simCard.operator())
            .addValue("tariff", simCard.tariff())
            .addValue("issuedToName", simCard.issuedToName())
            .addValue("locationName", simCard.locationName())
            .addValue("status", simCard.status());
        jdbcTemplate.update(
            "INSERT INTO sim_cards (id, tenant_id, phone_number, operator, tariff, issued_to_name, location_name, status) " +
            "VALUES (:id, :tenantId, :phoneNumber, :operator, :tariff, :issuedToName, :locationName, :status) " +
            "ON CONFLICT (tenant_id, phone_number) DO UPDATE SET " +
            "operator = :operator, tariff = :tariff, issued_to_name = :issuedToName, location_name = :locationName, status = :status",
            params
        );
        return simCard;
    }

    public void delete(UUID tenantId, UUID id) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        jdbcTemplate.update(
            "DELETE FROM sim_cards WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id)
        );
    }
}
