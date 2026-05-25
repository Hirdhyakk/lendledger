package com.lendledger.common.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    @Test
    void createsAndParsesAccessToken() {
        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-key-at-least-32-characters-long!!");
        JwtTokenProvider provider = new JwtTokenProvider(props);
        UUID userId = UUID.randomUUID();
        String token = provider.createAccessToken(userId, "a@test.com", Role.ADMIN);
        Claims claims = provider.parseToken(token);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("ADMIN", claims.get("role", String.class));
        assertTrue(provider.isAccessToken(claims));
    }
}
