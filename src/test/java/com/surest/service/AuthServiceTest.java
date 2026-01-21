package com.surest.service;

import com.surest.dto.AuthRequest;
import com.surest.dto.AuthResponse;
import com.surest.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsServiceImpl userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private AuthRequest request;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        request = new AuthRequest("admin_user", "AdminPass123");
        userDetails = User.withUsername("admin_user")
                .password("encoded") // password is irrelevant here; we don't verify it
                .authorities("ROLE_ADMIN")
                .build();
    }

    @Test
    void authenticateAndIssueToken_success() {
        // Arrange
        // Simulate successful authentication (any returned value is ignored by our code; no need to return an authenticated token)
        when(userDetailsService.loadUserByUsername("admin_user")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("mock-jwt-token");

        // Act
        AuthResponse response = authService.authenticateAndIssueToken(request);

        // Assert
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1))
                .loadUserByUsername("admin_user");
        verify(jwtUtil, times(1))
                .generateToken(userDetails);
    }

    @Test
    void authenticateAndIssueToken_badCredentials_propagates() {
        // Arrange
        doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Act & Assert
        assertThrows(BadCredentialsException.class,
                () -> authService.authenticateAndIssueToken(request));

        // Ensure that after a failure in authenticate, we do not attempt to load user or generate token
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void authenticateAndIssueToken_userLoadFails_propagates() {
        // Arrange
        // Auth succeeds, but loading user fails (e.g., DisabledException, UsernameNotFoundException, etc.)
        when(userDetailsService.loadUserByUsername("admin_user"))
                .thenThrow(new RuntimeException("User load failed"));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.authenticateAndIssueToken(request));
        assertEquals("User load failed", ex.getMessage());

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1)).loadUserByUsername("admin_user");
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void authenticateAndIssueToken_jwtGenerationFails_propagates() {
        // Arrange
        when(userDetailsService.loadUserByUsername("admin_user")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails))
                .thenThrow(new RuntimeException("JWT error"));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.authenticateAndIssueToken(request));
        assertEquals("JWT error", ex.getMessage());

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1)).loadUserByUsername("admin_user");
        verify(jwtUtil, times(1)).generateToken(userDetails);
    }
}