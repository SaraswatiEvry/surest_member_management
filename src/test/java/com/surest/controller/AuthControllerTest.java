
package com.surest.controller;

import com.surest.dto.AuthRequest;
import com.surest.dto.AuthResponse;
import com.surest.security.JwtUtil;
import com.surest.service.CustomUserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private CustomUserDetailsServiceImpl userDetailsService;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks private AuthController authController;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new User("admin_user", "AdminPass123", Collections.emptyList());
    }

    @Test
    void testLoginSuccess() {
        // Arrange
        AuthRequest req = new AuthRequest("admin_user", "AdminPass123");

        // Ensure authenticate() returns an *authenticated* token (3-arg ctor sets authenticated=true)
        UsernamePasswordAuthenticationToken authenticated =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticated);

        // Controller reloads the user; return our userDetails
        when(userDetailsService.loadUserByUsername(eq("admin_user"))).thenReturn(userDetails);

        // IMPORTANT: match by type (or by username) so stub applies even if different instance is passed
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("mock-jwt-token");

        // Act
        ResponseEntity<?> response = authController.login(req);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof AuthResponse);

        AuthResponse body = (AuthResponse) response.getBody();
        assertEquals("mock-jwt-token", body.getToken()); // ensure AuthResponse#getToken exists

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1)).loadUserByUsername("admin_user");
        verify(jwtUtil, times(1)).generateToken(any(UserDetails.class));
    }

    @Test
    void testLoginFailureInvalidCredentials() {
        // Arrange
        AuthRequest req = new AuthRequest("wrong", "bad");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act
        ResponseEntity<?> response = authController.login(req);

        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertEquals("Invalid credentials", response.getBody());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
    }
}
