package com.workshop.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Explicitly registers the /webjars/** resource handler so that
 * Spring Cloud Gateway (WebFlux) serves Swagger UI static assets
 * from the swagger-ui webjar on the classpath.
 */
@Configuration
public class WebFluxResourceConfig implements WebFluxConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
