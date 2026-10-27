package com.itam.assetservice.config;
import javax.sql.DataSource;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
@Profile("demo")
public class DemoSeedConfiguration {
    @Bean
    ApplicationRunner seedSyntheticDemo(DataSource dataSource) {
        return args -> {
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            String table = "assets";
            // One-time fixture on an isolated file database; reload preserves mutations.
            if (jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Long.class) == 0) {
                new ResourceDatabasePopulator(new ClassPathResource("demo/seed.sql")).execute(dataSource);
            }
        };
    }
}
