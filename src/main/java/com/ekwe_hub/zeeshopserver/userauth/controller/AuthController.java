package com.ekwe_hub.zeeshopserver.userauth.controller;

import com.ekwe_hub.zeeshopserver.shared.api.response.ApiResponse;
import com.ekwe_hub.zeeshopserver.userauth.dto.request.LoginRequest;
import com.ekwe_hub.zeeshopserver.userauth.dto.request.TokenRequest;
import com.ekwe_hub.zeeshopserver.userauth.dto.response.AuthResponse;
import com.ekwe_hub.zeeshopserver.userauth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request.usernameOrEmail(), request.password());
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody TokenRequest request) {
        AuthResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody TokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(@Valid @RequestBody TokenRequest request) {
        authService.logoutAll(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out of all sessions"));
    }
}
