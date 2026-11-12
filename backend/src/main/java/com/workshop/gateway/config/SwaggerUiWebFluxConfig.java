package com.workshop.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class SwaggerUiWebFluxConfig {

    @Bean
    public RouterFunction<ServerResponse> swaggerUiRouter() {
        return RouterFunctions
                .route(GET("/swagger-ui.html"), req -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .body(BodyInserters.fromResource(new ClassPathResource("static/swagger-ui.html"))))
                .andRoute(GET("/docs"), req -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .body(BodyInserters.fromResource(new ClassPathResource("static/swagger-ui.html"))));
    }
}

