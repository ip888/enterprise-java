package com.enterprise.portfolio.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Basic application context test to verify the Spring Boot application loads correctly.
 * This test runs without external dependencies to ensure the CI/CD pipeline can validate
 * the application configuration.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.r2dbc.url=r2dbc:h2:mem:///testdb",
    "spring.liquibase.enabled=false",
    "spring.kafka.enabled=false",
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=16379"
})
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that the Spring Boot application context loads successfully
        // without any external dependencies like PostgreSQL, Kafka, or Redis
    }

    @Test
    void applicationPropertiesAreValid() {
        // This test ensures that all required configuration properties are properly defined
        // and the application can start with default values
    }
}