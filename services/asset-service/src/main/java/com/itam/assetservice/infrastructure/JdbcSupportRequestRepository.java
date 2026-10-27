package com.itam.assetservice.infrastructure;

import com.itam.assetservice.domain.SupportRequest;
import com.itam.assetservice.domain.SupportRequestRepository;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSupportRequestRepository implements SupportRequestRepository {

    private static final String COLUMNS = "id, tenant_id, user_id, user_name, type, description, status, resolution_comment, created_at";

    private static final RowMapper<SupportRequest> ROW_MAPPER = (rs, rowNum) -> new SupportRequest(
        rs.getObject("id", UUID.class),
        rs.getObject("tenant_id", UUID.class),
        rs.getObject("user_id", UUID.class),
        rs.getString("user_name"),
        rs.getString("type"),
        rs.getString("description"),
        SupportRequest.SupportRequestStatus.valueOf(rs.getString("status")),
        rs.getString("resolution_comment"),
        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC) : null
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcSupportRequestRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public SupportRequest save(SupportRequest request) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(request.tenantId());
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", request.id())
            .addValue("tenantId", scopedTenantId)
            .addValue("userId", request.userId())
            .addValue("userName", request.userName())
            .addValue("type", request.type())
            .addValue("description", request.description())
            .addValue("status", request.status().name())
            .addValue("resolutionComment", request.resolutionComment());

        // Assuming created_at is handled by DB default or we pass it
        if (request.createdAt() != null) {
            params.addValue("createdAt", Timestamp.from(request.createdAt().toInstant()));
            jdbcTemplate.update(
                "INSERT INTO support_requests (" + COLUMNS + ") " +
                "VALUES (:id, :tenantId, :userId, :userName, :type, :description, :status, :resolutionComment, :createdAt) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "status = :status, resolution_comment = :resolutionComment",
                params
            );
        } else {
            jdbcTemplate.update(
                "INSERT INTO support_requests (id, tenant_id, user_id, user_name, type, description, status, resolution_comment) " +
                "VALUES (:id, :tenantId, :userId, :userName, :type, :description, :status, :resolutionComment) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "status = :status, resolution_comment = :resolutionComment",
                params
            );
        }
        return request;
    }

    @Override
    public List<SupportRequest> findByTenantId(UUID tenantId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT " + COLUMNS + " FROM support_requests WHERE tenant_id = :tenantId ORDER BY created_at DESC",
            Map.of("tenantId", scopedTenantId),
            ROW_MAPPER
        );
    }

    @Override
    public Optional<SupportRequest> findById(UUID tenantId, UUID id) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        List<SupportRequest> list = jdbcTemplate.query(
            "SELECT " + COLUMNS + " FROM support_requests WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id),
            ROW_MAPPER
        );
        return list.stream().findFirst();
    }
}
