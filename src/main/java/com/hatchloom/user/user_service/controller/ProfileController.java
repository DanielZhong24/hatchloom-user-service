package com.hatchloom.user.user_service.controller;

import com.hatchloom.user.user_service.dto.ProfileDTO;
import com.hatchloom.user.user_service.dto.UpdateProfileRequest;
import com.hatchloom.user.user_service.service.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/profile")
@Slf4j
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileDTO> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId) {

        log.info("Get profile request for user: {}", userId);
        log.debug("Authorization header received: {}", authHeader != null ? "Present (length: " + authHeader.length() + ")" : "Missing");

        String token = extractTokenFromHeader(authHeader);
        log.debug("Extracted token: {}", token != null ? "Present (length: " + token.length() + ")" : "Missing");

        if (token == null) {
            log.warn("No token provided in Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ProfileDTO profile = profileService.getProfile(token, UUID.fromString(userId));

        if (profile != null) {
            return ResponseEntity.ok(profile);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ProfileDTO> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String userId,
            @RequestBody UpdateProfileRequest request) {

        log.info("Update profile request for user: {}", userId);
        String token = extractTokenFromHeader(authHeader);
        ProfileDTO profile = profileService.updateProfile(token, UUID.fromString(userId), request);

        if (profile != null) {
            return ResponseEntity.ok(profile);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    public ResponseEntity<Page<ProfileDTO>> listProfiles(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Pageable pageable) {

        log.info("List profiles request");
        String token = extractTokenFromHeader(authHeader);
        Page<ProfileDTO> profiles = profileService.listProfiles(token, pageable);

        if (profiles.hasContent()) {
            return ResponseEntity.ok(profiles);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

