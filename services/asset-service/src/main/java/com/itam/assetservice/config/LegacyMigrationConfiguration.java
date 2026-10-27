package com.itam.assetservice.config;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Profile("legacy-v14")
public class LegacyMigrationConfiguration {
    @Bean
    FlywayMigrationStrategy requireExistingLegacyHistory() {
        return flyway -> {
            JdbcTemplate jdbc = new JdbcTemplate(flyway.getConfiguration().getDataSource());
            Integer found = jdbc.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = ? AND success = true", Integer.class, "14");
            if (found == null || found == 0) {
                throw new IllegalStateException("legacy-v14 requires an existing applied legacy history; use the default profile for a clean database");
            }
            // Validation still checks the exact original files; no repair or ignored checksums.
            flyway.migrate();
        };
    }
}
