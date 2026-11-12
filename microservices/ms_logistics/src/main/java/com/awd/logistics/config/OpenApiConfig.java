package com.awd.logistics.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI logisticsOpenApi(@Value("${server.port:8086}") String port) {
        return new OpenAPI()
                .info(new Info()
                        .title("Logistics Microservice API")
                        .version("1.0.0")
                        .description("REST API of the Logistics domain: deliveries and warehouses. " +
                                "The two resources are independent (no relation between their tables); " +
                                "a delivery only keeps the id of its order, which belongs to the Ordering microservice.")
                        .contact(new Contact().name("Ines")))
                .servers(List.of(new Server().url("http://localhost:" + port).description("Local")));
    }
}
