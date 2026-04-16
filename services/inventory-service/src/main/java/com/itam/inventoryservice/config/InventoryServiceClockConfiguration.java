package com.itam.inventoryservice.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryServiceClockConfiguration {

    @Bean
    public Clock inventoryServiceClock() {
        return Clock.systemUTC();
    }
}
