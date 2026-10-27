package com.itam.softwareservice.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SoftwareServiceClockConfiguration {

    @Bean
    public Clock softwareServiceClock() {
        return Clock.systemUTC();
    }
}
