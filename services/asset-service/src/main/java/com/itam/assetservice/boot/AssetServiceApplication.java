package com.itam.assetservice.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.itam.assetservice")
public class AssetServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssetServiceApplication.class, args);
    }
}
