package com.itam.softwareservice.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.itam.softwareservice")
public class SoftwareServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SoftwareServiceApplication.class, args);
    }
}
