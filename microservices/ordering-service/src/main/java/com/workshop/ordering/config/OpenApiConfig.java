package com.workshop.ordering.config;

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
    public OpenAPI orderingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ordering Service API")
                        .description("Order management microservice — create and track agricultural product orders.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Agriculture Team")
                                .email("team@agriculture.local")))
                .servers(List.of(
                        new Server().url("/").description("Default")));
    }
}

