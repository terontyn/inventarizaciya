package com.itam.assetservice.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssetServiceClockConfiguration {

    @Bean
    public Clock assetServiceClock() {
        return Clock.systemUTC();
    }
}
