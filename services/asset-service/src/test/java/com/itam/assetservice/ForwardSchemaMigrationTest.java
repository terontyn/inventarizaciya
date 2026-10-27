package com.itam.assetservice;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

class ForwardSchemaMigrationTest {
    @Test
    void upgradeFromPublicV7PreservesExistingAssetAndCreatesNoDemoRows() {
        var ds = new DriverManagerDataSource("jdbc:h2:mem:forward-schema;MODE=PostgreSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1", "sa", "");
        Flyway.configure().dataSource(ds).locations("classpath:db/migration").target("7").load().migrate();
        var jdbc = new JdbcTemplate(ds);
        jdbc.update("INSERT INTO assets(id,tenant_id,asset_tag,serial_number,asset_type,status) VALUES (?,?,?,?,?,?)",
                "a8000000-0000-0000-0000-000000000099", "00000000-0000-0000-0000-000000000099", "PRESERVE-099", "SYNTHETIC-099", "MONITOR", "ACTIVE");
        Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM assets", Integer.class));
        assertEquals("PRESERVE-099", jdbc.queryForObject("SELECT asset_tag FROM assets", String.class));
        for (String table : new String[]{"support_requests", "asset_types", "sim_cards", "cartridges"}) {
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class));
        }
        jdbc.update("INSERT INTO support_requests(id,tenant_id,user_id,user_name,type,description,status,resolution_comment) VALUES (?,?,?,?,?,?,?,?)",
                "a9000000-0000-0000-0000-000000000099", "00000000-0000-0000-0000-000000000099", "a1111111-1111-1111-1111-111111111111", "Synthetic user", "OTHER", "Synthetic request", "RESOLVED", "Preserve this resolution");
        // Re-running only the additive SQL simulates an already expanded schema, not checksum repair.
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/V15__asset_workspace_expansion.sql")).execute(ds);
        assertEquals("Preserve this resolution", jdbc.queryForObject("SELECT resolution_comment FROM support_requests", String.class));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM assets", Integer.class));
    }
}
