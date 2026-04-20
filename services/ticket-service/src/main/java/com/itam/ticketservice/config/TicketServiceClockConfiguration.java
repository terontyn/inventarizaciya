package com.itam.ticketservice.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TicketServiceClockConfiguration {

    @Bean
    public Clock ticketServiceClock() {
        return Clock.systemUTC();
    }
}
