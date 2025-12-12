
package com.surest.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Base64;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    // Use a deterministic 512-bit (64-byte) Base64-encoded key to satisfy HS512 requirements.
    // This decodes to 64 zero bytes—fine for unit tests (not for production).
    private final String testSecret = Base64.getEncoder()
            .encodeToString(new byte[64]);

    @BeforeEach
    void setUp() {
        // 10 hours expiration for normal tests
        jwtUtil = new JwtUtil(testSecret, 1000L * 60 * 60 * 10);
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
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void testValidateToken_InvalidUsername() {
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        String token = jwtUtil.generateToken(userDetails);
        UserDetails otherUser = new User("otherUser", "password", Collections.emptyList());
        assertFalse(jwtUtil.validateToken(token, otherUser));
    }

    @Test
    void testValidateToken_ExpiredToken() throws InterruptedException {
        // 1-second expiration for this test
        JwtUtil shortExpiryJwtUtil = new JwtUtil(testSecret, 1000L);
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        String token = shortExpiryJwtUtil.generateToken(userDetails);

        Thread.sleep(1500); // wait until it expires
        assertFalse(shortExpiryJwtUtil.validateToken(token, userDetails));
    }
}
