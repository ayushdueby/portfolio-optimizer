package com.example.portfolio.controller;

import com.example.portfolio.model.UserProfile;
import com.example.portfolio.service.UserProfileService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping("/profile")
    public ResponseEntity<SessionResponse> saveProfile(@RequestBody UserProfile profile) {
        UserProfile saved = userProfileService.saveProfile(profile);
        SessionResponse response = new SessionResponse();
        response.setSessionId(saved.getSessionId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/{sessionId}")
    public ResponseEntity<UserProfile> getProfile(@PathVariable String sessionId) {
        UserProfile profile = userProfileService.getBySessionId(sessionId);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    @Data
    public static class SessionResponse {
        private String sessionId;
    }
}

