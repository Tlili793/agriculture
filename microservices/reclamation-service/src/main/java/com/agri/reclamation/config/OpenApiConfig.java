package com.agri.reclamation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reclamationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reclamation Service API")
                        .description("Complaint management microservice — create reclamations, follow their status, and manage admin responses.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Agriculture Team")
                                .email("team@agriculture.local")))
                .servers(List.of(
                        new Server().url("/").description("Default")));
    }
}
