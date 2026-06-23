package com.cafe.inventory.controller.api;

import com.cafe.inventory.dto.AuthDtos.*;
import com.cafe.inventory.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login and receive a JWT token")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Get the current authenticated user")
    @GetMapping("/me")
    public ResponseEntity<?> me(java.security.Principal principal,
                                org.springframework.security.core.Authentication auth) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String role = auth.getAuthorities().stream()
                .findFirst().map(a -> a.getAuthority()).orElse("ROLE_VIEWER");
        return ResponseEntity.ok(java.util.Map.of(
                "username", principal.getName(),
                "role", role));
    }
}
