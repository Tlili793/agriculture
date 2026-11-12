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

class ValidateDeliveryAssignmentGatewayFilterFactoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GatewayFilter filter = new ValidateDeliveryAssignmentGatewayFilterFactory(objectMapper)
            .apply(new ValidateDeliveryAssignmentGatewayFilterFactory.Config());

    @Test
    void forwardsWhenTransporterIdMatchesToken() {
        UUID transporterId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.post("/api/deliveries/42/assignment-request")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"transporterId":"%s"}
                        """.formatted(transporterId)), transporterId);
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
        assertEquals(transporterId.toString(), forwardedBody.get().get("transporterId").asText());
    }

    @Test
    void rejectsWhenTransporterIdDiffersFromToken() {
        UUID tokenTransporterId = UUID.randomUUID();
        UUID payloadTransporterId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.post("/api/deliveries/42/assignment-request")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"transporterId":"%s"}
                        """.formatted(payloadTransporterId)), tokenTransporterId);
        AtomicBoolean invoked = new AtomicBoolean();

        filter.filter(exchange, forwardedExchange -> {
            invoked.set(true);
            return Mono.empty();
        }).block();

        assertFalse(invoked.get());
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void rejectsMissingTransporterId() {
        UUID transporterId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.post("/api/deliveries/42/assignment-request")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}"), transporterId);

        filter.filter(exchange, forwardedExchange -> Mono.empty()).block();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
    }

    @Test
    void rejectsInvalidTransporterId() {
        UUID transporterId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(MockServerHttpRequest.post("/api/deliveries/42/assignment-request")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"transporterId":"not-a-uuid"}
                        """), transporterId);

        filter.filter(exchange, forwardedExchange -> Mono.empty()).block();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
    }

    @Test
    void rejectsMissingBody() {
        UUID transporterId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(
                MockServerHttpRequest.post("/api/deliveries/42/assignment-request").build(), transporterId);

        filter.filter(exchange, forwardedExchange -> Mono.empty()).block();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
    }

    @Test
    void rejectsMissingPrincipal() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/deliveries/42/assignment-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"transporterId":"%s"}
                                """.formatted(UUID.randomUUID())));

        filter.filter(exchange, forwardedExchange -> Mono.empty()).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void ignoresUnrelatedPaths() {
        UUID transporterId = UUID.randomUUID();
        MockServerWebExchange exchange = exchange(
                MockServerHttpRequest.get("/api/deliveries").build(), transporterId);
        AtomicBoolean invoked = new AtomicBoolean();

        filter.filter(exchange, forwardedExchange -> {
            invoked.set(true);
            return Mono.empty();
        }).block();

        assertTrue(invoked.get());
    }

    private MockServerWebExchange exchange(MockServerHttpRequest request, UUID transporterId) {
        return MockServerWebExchange.builder(request)
                .principal(new JwtAuthenticationToken(jwt(transporterId)))
                .build();
    }

    private Jwt jwt(UUID transporterId) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", transporterId.toString())
                .build();
    }
}
