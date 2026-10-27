package com.itam.userservice.infrastructure;

import com.itam.userservice.domain.Tenant;
import com.itam.userservice.domain.TenantRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTenantRepository implements TenantRepository {

    private static final RowMapper<Tenant> TENANT_ROW_MAPPER = (resultSet, rowNum) -> new Tenant(
        resultSet.getObject("id", UUID.class),
        resultSet.getString("name")
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcTenantRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Tenant create(String name) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO tenants (id, name) VALUES (:id, :name)",
            Map.of("id", id, "name", name)
        );
        return new Tenant(id, name);
    }

    @Override
    public Optional<Tenant> findById(UUID id) {
        List<Tenant> tenants = jdbcTemplate.query(
            "SELECT id, name FROM tenants WHERE id = :id",
            Map.of("id", id),
            TENANT_ROW_MAPPER
        );
        return tenants.stream().findFirst();
    }
}
