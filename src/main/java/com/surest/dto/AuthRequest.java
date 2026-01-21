package com.surest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Prevent leaking password in logs
    @Override
    public String toString() {
        return "AuthRequest{username='" + username + "', password='[PROTECTED]'}";
    }
}
