package com.surest.controller;

import com.surest.dto.AuthRequest;
import com.surest.dto.AuthResponse;
import com.surest.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void testLoginSuccess() {
        // Arrange
        AuthRequest req = new AuthRequest("admin_user", "AdminPass123");
        AuthResponse authResponse = new AuthResponse("mock-jwt-token");

        when(authService.authenticateAndIssueToken(any(AuthRequest.class)))
                .thenReturn(authResponse);

        // Act
        ResponseEntity<AuthResponse> response = authController.login(req);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("mock-jwt-token", response.getBody().getToken());

        verify(authService, times(1)).authenticateAndIssueToken(req);
    }

    @Test
    void testLoginFailureInvalidCredentials() {
        // Arrange
        AuthRequest req = new AuthRequest("wrong", "bad");
        when(authService.authenticateAndIssueToken(any(AuthRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        // In a pure unit test (no ControllerAdvice), the controller bubbles the exception.
        assertThrows(BadCredentialsException.class, () -> authController.login(req));

        verify(authService, times(1)).authenticateAndIssueToken(req);
    }
}