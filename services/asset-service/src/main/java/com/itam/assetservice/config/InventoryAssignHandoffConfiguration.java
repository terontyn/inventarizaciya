package com.itam.assetservice.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;

@Configuration
public class InventoryAssignHandoffConfiguration {

    @Bean(name = "inventoryAssignHandoffExecutor")
    public Executor inventoryAssignHandoffExecutor(Environment environment) {
        if (environment.acceptsProfiles(Profiles.of("test"))) {
            return new SyncTaskExecutor();
        }
        return new SimpleAsyncTaskExecutor("inventory-assign-handoff-");
    }
}
