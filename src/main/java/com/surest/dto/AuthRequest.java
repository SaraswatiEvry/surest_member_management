package com.surest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
