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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidateOrderUserIdGatewayFilterFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GatewayFilter filter = new ValidateOrderUserIdGatewayFilterFactory(objectMapper)
            .apply(new ValidateOrderUserIdGatewayFilterFactory.Config());

    @Test
    void forwardsRequestWhenUserIdMatchesToken() {
        UUID userId = UUID.randomUUID();
        String body = """
                {"userId":"%s","items":[{"productId":1,"quantity":2,"unitPrice":10.00}]}
                """.formatted(userId);
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.post("/api/orders")
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
        assertEquals(2, forwardedBody.get().get("items").get(0).get("quantity").asInt());
    }

    @Test
    void rejectsMismatchedUserId() {
        UUID tokenUserId = UUID.randomUUID();
        UUID payloadUserId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"userId":"%s","items":[]}
                        """.formatted(payloadUserId)), tokenUserId);
        AtomicBoolean invoked = new AtomicBoolean();

        filter.filter(exchange, forwardedExchange -> {
            invoked.set(true);
            return Mono.empty();
        }).block();

        assertFalse(invoked.get());
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void rejectsMissingUserId() {
        UUID userId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"items":[]}
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
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.post("/api/orders")
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
                MockServerHttpRequest.get("/api/orders/user/%s".formatted(userId)).build(), userId);
        AtomicBoolean invoked = new AtomicBoolean();

        filter.filter(exchange, forwardedExchange -> {
            invoked.set(true);
            return Mono.empty();
        }).block();

        assertTrue(invoked.get());
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
