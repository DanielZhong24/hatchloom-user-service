package com.hatchloom.user.user_service.controller;

import com.hatchloom.user.user_service.dto.*;
import com.hatchloom.user.user_service.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        log.info("Registration request for username: {}", request.getUsername());
        RegisterResponse response = authService.register(request);

        if (response.getUserId() != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("Login request for username: {}", request.getUsername());
        LoginResponse response = authService.login(request);

        if (response.getAccessToken() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request");
        LoginResponse response = authService.refreshAccessToken(request);

        if (response.getAccessToken() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<SessionValidationResponse> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = extractTokenFromHeader(authHeader);
        SessionValidationResponse response = authService.validateSessionToken(token);

        if (response.isValid()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/permissions")
    public ResponseEntity<RolePermissionDTO> getRolePermissions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String token = extractTokenFromHeader(authHeader);
        RolePermissionDTO response = authService.getRolePermissions(token);

        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/link-parent/{studentId}")
    public ResponseEntity<String> linkParentToStudent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String studentId) {

        String token = extractTokenFromHeader(authHeader);
        boolean success = authService.linkParentToStudent(token, java.util.UUID.fromString(studentId));

        if (success) {
            return ResponseEntity.ok("Parent linked successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to link parent");
        }
    }

    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

