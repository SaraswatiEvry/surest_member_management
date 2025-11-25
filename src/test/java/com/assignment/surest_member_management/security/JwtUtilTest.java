package com.assignment.surest_member_management.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void testGenerateToken_NotNull() {
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());

        String token = jwtUtil.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testExtractUsername() {
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        String token = jwtUtil.generateToken(userDetails);

        String username = jwtUtil.extractUsername(token);

        assertEquals("testUser", username);
    }

    @Test
    void testValidateToken_ValidToken() {
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        String token = jwtUtil.generateToken(userDetails);

        boolean isValid = jwtUtil.validateToken(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidUsername() {
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        String token = jwtUtil.generateToken(userDetails);

        UserDetails otherUser = new User("otherUser", "password", Collections.emptyList());

        boolean isValid = jwtUtil.validateToken(token, otherUser);

        assertFalse(isValid);
    }

    @Test
    void testValidateToken_ExpiredToken() throws InterruptedException {
        // Generate token with short expiration by temporarily modifying JwtUtil
        JwtUtil shortExpiryJwtUtil = new JwtUtil() {
            @Override
            public String generateToken(UserDetails userDetails) {
                return io.jsonwebtoken.Jwts.builder()
                        .setSubject(userDetails.getUsername())
                        .setIssuedAt(new java.util.Date())
                        .setExpiration(new java.util.Date(System.currentTimeMillis() + 1000)) // 1 second expiry
                        .signWith(io.jsonwebtoken.SignatureAlgorithm.HS512, getSigningKey())
                        .compact();
            }
        };

        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        String token = shortExpiryJwtUtil.generateToken(userDetails);

        Thread.sleep(1500); // Wait for token to expire

        boolean isValid = shortExpiryJwtUtil.validateToken(token, userDetails);

        assertFalse(isValid);
    }
}

