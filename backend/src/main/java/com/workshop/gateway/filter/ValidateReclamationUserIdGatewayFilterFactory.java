package com.workshop.gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class ValidateReclamationUserIdGatewayFilterFactory
        extends AbstractGatewayFilterFactory<ValidateReclamationUserIdGatewayFilterFactory.Config> {

    private static final String RECLAMATIONS_PATH = "/api/reclamations";
    private static final String USER_PATH_PREFIX = "/api/reclamations/user/";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private final ObjectMapper objectMapper;

    public ValidateReclamationUserIdGatewayFilterFactory(ObjectMapper objectMapper) {
        super(Config.class);
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            if (request.getMethod() == HttpMethod.POST && RECLAMATIONS_PATH.equals(path)) {
                return validateCreateBody(exchange, chain);
            }

            if (request.getMethod() == HttpMethod.GET && path.startsWith(USER_PATH_PREFIX)) {
                return validateUserPath(exchange, chain, path.substring(USER_PATH_PREFIX.length()));
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> validateCreateBody(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .flatMap(authentication -> validateBody(exchange, chain, authentication).thenReturn(true))
                .switchIfEmpty(Mono.defer(() -> writeProblem(
                        exchange,
                        HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        "Authentication is required"
                ).thenReturn(true)))
                .then();
    }

    private Mono<Void> validateBody(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            JwtAuthenticationToken authentication
    ) {
        String subject = authentication.getToken().getSubject();
        if (subject == null || subject.isBlank()) {
            return writeProblem(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized", "Authentication is required");
        }

        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    byte[] body = new byte[dataBuffer.readableByteCount()];
                    try {
                        dataBuffer.read(body);
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }

                    JsonNode payload;
                    try {
                        payload = objectMapper.readTree(body);
                    } catch (Exception exception) {
                        return writeProblem(exchange, HttpStatus.BAD_REQUEST, "Invalid request",
                                "Request body must be valid JSON").thenReturn(true);
                    }

                    JsonNode userIdNode = payload == null ? null : payload.get("userId");
                    if (userIdNode == null || !userIdNode.isTextual() || userIdNode.asText().isBlank()) {
                        return writeProblem(exchange, HttpStatus.BAD_REQUEST, "Invalid request",
                                "userId is required").thenReturn(true);
                    }

                    UUID payloadUserId;
                    UUID tokenUserId;
                    try {
                        payloadUserId = UUID.fromString(userIdNode.asText());
                        tokenUserId = UUID.fromString(subject);
                    } catch (IllegalArgumentException exception) {
                        return writeProblem(exchange, HttpStatus.BAD_REQUEST, "Invalid request",
                                "userId must be a valid UUID").thenReturn(true);
                    }

                    if (!payloadUserId.equals(tokenUserId)) {
                        return writeProblem(exchange, HttpStatus.FORBIDDEN, "Forbidden",
                                "userId does not match the authenticated user").thenReturn(true);
                    }

                    ServerHttpRequest decoratedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            return Flux.defer(() -> Flux.just(exchange.getResponse().bufferFactory().wrap(body)));
                        }

                        @Override
                        public HttpHeaders getHeaders() {
                            HttpHeaders headers = new HttpHeaders();
                            headers.putAll(super.getHeaders());
                            headers.remove(HttpHeaders.TRANSFER_ENCODING);
                            headers.setContentLength(body.length);
                            return headers;
                        }
                    };

                    return chain.filter(exchange.mutate().request(decoratedRequest).build()).thenReturn(true);
                })
                .switchIfEmpty(Mono.defer(() -> writeProblem(
                        exchange,
                        HttpStatus.BAD_REQUEST,
                        "Invalid request",
                        "Request body is required"
                ).thenReturn(true)))
                .then();
    }

    private Mono<Void> validateUserPath(ServerWebExchange exchange, GatewayFilterChain chain, String pathUserId) {
        return exchange.getPrincipal()
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .flatMap(authentication -> validateUser(exchange, chain, authentication, pathUserId).thenReturn(true))
                .switchIfEmpty(Mono.defer(() -> writeProblem(
                        exchange,
                        HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        "Authentication is required"
                ).thenReturn(true)))
                .then();
    }

    private Mono<Void> validateUser(
            ServerWebExchange exchange,
            GatewayFilterChain chain,
            JwtAuthenticationToken authentication,
            String pathUserId
    ) {
        if (isAdmin(authentication)) {
            return chain.filter(exchange);
        }

        String subject = authentication.getToken().getSubject();
        if (subject == null || subject.isBlank()) {
            return writeProblem(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized",
                    "Authentication is required");
        }

        UUID requestedUserId;
        UUID tokenUserId;
        try {
            requestedUserId = UUID.fromString(pathUserId);
            tokenUserId = UUID.fromString(subject);
        } catch (IllegalArgumentException exception) {
            return writeProblem(exchange, HttpStatus.BAD_REQUEST, "Invalid request",
                    "userId must be a valid UUID");
        }

        if (!requestedUserId.equals(tokenUserId)) {
            return writeProblem(exchange, HttpStatus.FORBIDDEN, "Forbidden",
                    "userId does not match the authenticated user");
        }

        return chain.filter(exchange);
    }

    private boolean isAdmin(JwtAuthenticationToken authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> ADMIN_ROLE.equals(authority.getAuthority()));
    }

    private Mono<Void> writeProblem(
            ServerWebExchange exchange,
            HttpStatus status,
            String title,
            String detail
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);

        byte[] body;
        try {
            body = objectMapper.writeValueAsBytes(problem);
        } catch (Exception exception) {
            body = new byte[0];
        }

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        byte[] responseBody = body;
        return response.writeWith(Mono.just(response.bufferFactory().wrap(responseBody)));
    }

    public static class Config {
    }
}
