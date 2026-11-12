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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidateReclamationUserIdGatewayFilterFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GatewayFilter filter = new ValidateReclamationUserIdGatewayFilterFactory(objectMapper)
            .apply(new ValidateReclamationUserIdGatewayFilterFactory.Config());

    @Test
    void forwardsCreateWhenUserIdMatchesToken() {
        UUID userId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.post("/api/reclamations")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"userId":"%s","orderId":1,"subject":"Broken","description":"Damaged"}
                        """.formatted(userId)), userId);
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
    }

    @Test
    void rejectsCreateWhenUserIdDiffersFromToken() {
        UUID tokenUserId = UUID.randomUUID();
        UUID payloadUserId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.post("/api/reclamations")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"userId":"%s","orderId":1,"subject":"Broken","description":"Damaged"}
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
    void forwardsOwnUserList() {
        UUID userId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(
                MockServerHttpRequest.get("/api/reclamations/user/%s".formatted(userId)).build(), userId);
        AtomicBoolean invoked = new AtomicBoolean();

        filter.filter(exchange, forwardedExchange -> {
            invoked.set(true);
            return Mono.empty();
        }).block();

        assertTrue(invoked.get());
    }

    @Test
    void rejectsAnotherUsersList() {
        UUID tokenUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(
                MockServerHttpRequest.get("/api/reclamations/user/%s".formatted(otherUserId)).build(), tokenUserId);
        AtomicBoolean invoked = new AtomicBoolean();

        filter.filter(exchange, forwardedExchange -> {
            invoked.set(true);
            return Mono.empty();
        }).block();

        assertFalse(invoked.get());
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void adminCanReadAnyUserList() {
        UUID adminId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        MockServerWebExchange exchange = MockServerWebExchange.builder(
                        MockServerHttpRequest.get("/api/reclamations/user/%s".formatted(otherUserId)).build())
                .principal(new JwtAuthenticationToken(jwt(adminId),
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .build();
        AtomicBoolean invoked = new AtomicBoolean();

        filter.filter(exchange, forwardedExchange -> {
            invoked.set(true);
            return Mono.empty();
        }).block();

        assertTrue(invoked.get());
    }

    @Test
    void ignoresUnrelatedPaths() {
        UUID userId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(
                MockServerHttpRequest.get("/api/reclamations").build(), userId);
        AtomicBoolean invoked = new AtomicBoolean();

        filter.filter(exchange, forwardedExchange -> {
            invoked.set(true);
            return Mono.empty();
        }).block();

        assertTrue(invoked.get());
    }

    private MockServerWebExchange exchange(MockServerHttpRequest request, UUID userId) {
        return MockServerWebExchange.builder(request)
                .principal(new JwtAuthenticationToken(jwt(userId)))
                .build();
    }

    private Jwt jwt(UUID userId) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", userId.toString())
                .build();
    }
}
