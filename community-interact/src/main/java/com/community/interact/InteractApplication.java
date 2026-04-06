package com.community.interact;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.community")
@EnableDiscoveryClient
public class InteractApplication {
    public static void main(String[] args) {
        SpringApplication.run(InteractApplication.class, args);
    }
}
