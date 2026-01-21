package com.surest.service;

import com.surest.dto.AuthRequest;
import com.surest.dto.AuthResponse;
import com.surest.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    /**
     * Unit of work:
     * 1) authenticate
     * 2) load user
     * 3) generate JWT
     * <p>
     * Throws AuthenticationException (e.g., BadCredentialsException) on failure.
     */
    public AuthResponse authenticateAndIssueToken(AuthRequest request) {
        // Let AuthenticationException bubble up to the global exception handler
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String jwt = jwtUtil.generateToken(userDetails);

        return new AuthResponse(jwt);
    }
}