package com.cafe.inventory.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthDtos {

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}

    public record LoginResponse(
            String token,
            String username,
            String role,
            long expiresInMs
    ) {}
}
