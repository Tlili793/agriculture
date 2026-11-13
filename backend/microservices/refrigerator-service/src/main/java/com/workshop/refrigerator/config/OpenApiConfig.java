package com.workshop.refrigerator.config;

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
    public OpenAPI refrigeratorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Refrigerator Service API")
                        .description("Food inventory microservice — manage fruits, vegetables, meals, and monthly budgets.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Agriculture Team")
                                .email("team@agriculture.local")))
                .servers(List.of(
                        new Server().url("/").description("Default")));
    }
}

