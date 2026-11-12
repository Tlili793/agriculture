package com.workshop.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakJwtAuthenticationConverterTest {

    private final KeycloakJwtAuthenticationConverter converter = new KeycloakJwtAuthenticationConverter();

    @Test
    void mapsRealmRolesToRoleAuthorities() {
        Jwt jwt = jwt(Map.of("roles", List.of("ADMIN", "USER")));

        AbstractAuthenticationToken authentication = converter.convert(jwt).block();

        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .sorted()
                .collect(Collectors.toList());
        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), authorities);
    }

    @Test
    void ignoresMissingRealmAccess() {
        Jwt jwt = jwt(null);

        AbstractAuthenticationToken authentication = converter.convert(jwt).block();

        assertTrue(authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .noneMatch(authority -> authority.startsWith("ROLE_")));
    }

    private Jwt jwt(Map<String, Object> realmAccess) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", UUID.randomUUID().toString());
        if (realmAccess != null) {
            builder.claim("realm_access", realmAccess);
        }
        return builder.build();
    }
}
