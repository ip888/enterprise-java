package com.enterprise.portfolio.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * User Service Application
 * 
 * Enterprise-grade user management service demonstrating:
 * - Event-driven architecture with Apache Kafka
 * - Reactive programming with Spring WebFlux
 * - Modern Java 21 features
 * - R2DBC reactive database access
 * - JWT-based authentication and authorization
 * 
 * @author Enterprise Portfolio
 * @version 1.0.0
 */
@SpringBootApplication
@EnableR2dbcAuditing
@EnableKafka
public class UserServiceApplication {

    public static void main(String[] args) {
        // Enable Java 21 Virtual Threads for improved performance
        System.setProperty("spring.threads.virtual.enabled", "true");
        
        SpringApplication.run(UserServiceApplication.class, args);
    }
}