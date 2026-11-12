package com.workshop.gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidateMarketplaceUserIdGatewayFilterFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GatewayFilter filter = new ValidateMarketplaceUserIdGatewayFilterFactory(objectMapper)
            .apply(new ValidateMarketplaceUserIdGatewayFilterFactory.Config());

    @Test
    void forwardsCreateWhenUserIdMatchesToken() {
        UUID userId = UUID.randomUUID();
        String body = listingBody(userId, "Fresh tomatoes");
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.post("/api/marketplace/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body), userId);
        AtomicReference<JsonNode> forwardedBody = new AtomicReference<>();
        AtomicBoolean invoked = new AtomicBoolean();

        GatewayFilterChain chain = forwardedExchange -> {
            invoked.set(true);
            return DataBufferUtils.join(forwardedExchange.getRequest().getBody())
                    .doOnNext(dataBuffer -> {
                        try {
                            forwardedBody.set(objectMapper.readTree(dataBuffer.asInputStream()));
                        } catch (Exception exception) {
                            throw new IllegalStateException(exception);
                        } finally {
                            DataBufferUtils.release(dataBuffer);
                        }
                    })
                    .then();
        };

        filter.filter(exchange, chain).block();

        assertTrue(invoked.get());
        assertNotNull(forwardedBody.get());
        assertEquals(userId.toString(), forwardedBody.get().get("userId").asText());
        assertEquals("Fresh tomatoes", forwardedBody.get().get("title").asText());
    }

    @Test
    void rejectsCreateWithMismatchedUserId() {
        UUID tokenUserId = UUID.randomUUID();
        UUID payloadUserId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.post("/api/marketplace/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(listingBody(payloadUserId, "Fresh tomatoes")), tokenUserId);
        AtomicBoolean invoked = new AtomicBoolean();

        filter.filter(exchange, forwardedExchange -> {
            invoked.set(true);
            return Mono.empty();
        }).block();

        assertFalse(invoked.get());
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void rejectsCreateWithMissingUserId() {
        UUID userId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.post("/api/marketplace/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"title":"Fresh tomatoes","price":2.50,"quantity":10}
                        """), userId);
        AtomicBoolean invoked = new AtomicBoolean();

        filter.filter(exchange, forwardedExchange -> {
            invoked.set(true);
            return Mono.empty();
        }).block();

        assertFalse(invoked.get());
        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
    }

    @Test
    void rejectsInvalidJson() {
        UUID userId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.post("/api/marketplace/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .body("not-json"), userId);
        AtomicBoolean invoked = new AtomicBoolean();

        filter.filter(exchange, forwardedExchange -> {
            invoked.set(true);
            return Mono.empty();
        }).block();

        assertFalse(invoked.get());
        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
    }

    @Test
    void ignoresGetRequests() {
        UUID userId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(
                MockServerHttpRequest.get("/api/marketplace/listings/user/%s".formatted(userId)).build(), userId);
        AtomicBoolean invoked = new AtomicBoolean();

        filter.filter(exchange, forwardedExchange -> {
            invoked.set(true);
            return Mono.empty();
        }).block();

        assertTrue(invoked.get());
    }

    @Test
    void forwardsUpdateWhenUserIdMatchesToken() {
        UUID userId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.put("/api/marketplace/listings/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(listingBody(userId, "Organic tomatoes")), userId);
        AtomicBoolean invoked = new AtomicBoolean();

        filter.filter(exchange, forwardedExchange -> {
            invoked.set(true);
            return Mono.empty();
        }).block();

        assertTrue(invoked.get());
    }

    @Test
    void forwardsDeleteWhenQueryUserIdMatchesToken() {
        UUID userId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(
                MockServerHttpRequest.delete("/api/marketplace/listings/1")
                        .queryParam("userId", userId.toString())
                        .build(), userId);
        AtomicBoolean invoked = new AtomicBoolean();

        filter.filter(exchange, forwardedExchange -> {
            invoked.set(true);
            return Mono.empty();
        }).block();

        assertTrue(invoked.get());
    }

    @Test
    void rejectsDeleteWithoutUserId() {
        UUID userId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(
                MockServerHttpRequest.delete("/api/marketplace/listings/1").build(), userId);
        AtomicBoolean invoked = new AtomicBoolean();

        filter.filter(exchange, forwardedExchange -> {
            invoked.set(true);
            return Mono.empty();
        }).block();

        assertFalse(invoked.get());
        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
    }

    @Test
    void rejectsDeleteWithMismatchedUserId() {
        UUID tokenUserId = UUID.randomUUID();
        UUID queryUserId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(
                MockServerHttpRequest.delete("/api/marketplace/listings/1")
                        .queryParam("userId", queryUserId.toString())
                        .build(), tokenUserId);
        AtomicBoolean invoked = new AtomicBoolean();

        filter.filter(exchange, forwardedExchange -> {
            invoked.set(true);
            return Mono.empty();
        }).block();

        assertFalse(invoked.get());
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    private String listingBody(UUID userId, String title) {
        return """
                {"userId":"%s","title":"%s","description":"Farm fresh","price":2.50,"quantity":10}
                """.formatted(userId, title);
    }

    private MockServerWebExchange exchange(MockServerHttpRequest request, UUID userId) {
        return MockServerWebExchange.builder(request)
                .principal(new JwtAuthenticationToken(Jwt.withTokenValue("token")
                        .header("alg", "none")
                        .claim("sub", userId.toString())
                        .build()))
                .build();
    }
}
