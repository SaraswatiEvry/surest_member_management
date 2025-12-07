
package com.surest.controller;

import com.surest.dto.AuthRequest;
import com.surest.dto.AuthResponse;
import com.surest.service.CustomUserDetailsServiceImpl;
import com.surest.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsServiceImpl userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    public AuthControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        userDetailsService = mock(CustomUserDetailsServiceImpl.class);
        jwtUtil = mock(JwtUtil.class);

        authController = new AuthController(authenticationManager, userDetailsService, jwtUtil);
    }

    @Test
    void testLoginSuccess() {
        // Arrange
        AuthRequest req = new AuthRequest("admin_user", "AdminPass123");
        UserDetails userDetails = new User("admin_user", "AdminPass123", Collections.emptyList());

        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(userDetails, null));
        when(userDetailsService.loadUserByUsername("admin_user")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("mock-jwt-token");

        // Act
        ResponseEntity<?> response = authController.login(req);

        // Assert
        assertNotNull(response.getBody());
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("mock-jwt-token", authResponse.getToken());
        assertEquals(200, response.getStatusCode().value());

        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtUtil, times(1)).generateToken(any());
    }

    @Test
    void testLoginFailureInvalidCredentials() {
        // Arrange
        AuthRequest req = new AuthRequest("wrong", "bad");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act
        ResponseEntity<?> response = authController.login(req);

        // Assert
        assertEquals(401, response.getStatusCode().value());
        assertEquals("Invalid credentials", response.getBody());

        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtUtil, never()).generateToken(any());
    }
}
