package com.example.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Movie Library API",
                version = "1.0.0",
                description = """
                        A secure REST API for managing a movie library with background rating enrichment.
                        
                        ## Features
                        - CRUD operations for movies
                        - Automatic rating enrichment from OMDb API
                        - Role-based access control (ADMIN/USER)
                        
                        ## Authentication
                        This API uses HTTP Basic Authentication.
                        
                        **Test Credentials:**
                        - Admin: `admin` / `admin123` (full access)
                        - User: `user` / `user123` (read-only access)
                        
                        ## Rating Enrichment
                        When a movie is created, the system asynchronously fetches its rating from the OMDb API.
                        The rating status can be:
                        - `PENDING`: Rating lookup in progress
                        - `ENRICHED`: Rating successfully retrieved
                        - `NOT_FOUND`: Movie not found in OMDb
                        - `ERROR`: Error during rating lookup
                        """,
                contact = @Contact(
                        name = "API Support",
                        email = "support@example.com"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local development server")
        }
)
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        description = "HTTP Basic Authentication"
)
public class OpenApiConfig {
    // Configuration is done via annotations
}

