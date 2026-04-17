package com.itam.maintenanceservice.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MaintenanceServiceClockConfiguration {

    @Bean
    public Clock maintenanceServiceClock() {
        return Clock.systemUTC();
    }
}
