package com.surest.controller;

import com.surest.dto.AuthRequest;
import com.surest.dto.AuthResponse;
import com.surest.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        // Any AuthenticationException will bubble up to GlobalExceptionHandler
        AuthResponse response = authService.authenticateAndIssueToken(request);
        return ResponseEntity.ok(response);
    }
}