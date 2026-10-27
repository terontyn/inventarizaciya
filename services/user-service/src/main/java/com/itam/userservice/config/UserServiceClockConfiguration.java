package com.itam.userservice.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserServiceClockConfiguration {

    @Bean
    public Clock userServiceClock() {
        return Clock.systemUTC();
    }
}
