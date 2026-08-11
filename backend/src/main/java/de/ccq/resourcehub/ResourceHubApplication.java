package de.ccq.resourcehub;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * ResourceHub Backend Application
 * 
 * Main entry point for the Spring Boot application.
 */
@SpringBootApplication
public class ResourceHubApplication {

    @Bean
    Clock systemClock() {
        return Clock.systemUTC();
    }

    public static void main(String[] args) {
        SpringApplication.run(ResourceHubApplication.class, args);
    }
}
