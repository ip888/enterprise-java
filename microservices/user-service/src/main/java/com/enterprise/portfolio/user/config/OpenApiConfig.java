package com.enterprise.portfolio.user.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Enterprise User Service API")
                        .version("1.0.0")
                        .description("""
                                Enterprise-grade User Management Service built with Spring Boot 3.2, Java 21, and WebFlux.
                                
                                **Features:**
                                - JWT-based authentication
                                - Reactive programming with R2DBC
                                - User registration and profile management
                                - Role-based access control
                                - PostgreSQL database integration
                                - Health monitoring with Actuator
                                
                                **Authentication:**
                                1. Register a new user via POST /api/v1/auth/register
                                2. Login via POST /api/v1/auth/login to get JWT token
                                3. Use the token in Authorization header: `Bearer <token>`
                                """)
                        .contact(new Contact()
                                .name("Enterprise Portfolio Team")
                                .email("admin@enterprise-portfolio.com")
                                .url("https://github.com/ip888/enterprise-java"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server"),
                        new Server()
                                .url("https://api.enterprise-portfolio.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Token Authentication")));
    }
}