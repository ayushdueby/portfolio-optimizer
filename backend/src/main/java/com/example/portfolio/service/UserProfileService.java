package com.example.portfolio.service;

import com.example.portfolio.model.UserProfile;
import com.example.portfolio.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfile saveProfile(UserProfile profile) {
        if (profile.getSessionId() == null || profile.getSessionId().isBlank()) {
            profile.setSessionId(UUID.randomUUID().toString());
        }
        return userProfileRepository.save(profile);
    }

    public UserProfile getBySessionId(String sessionId) {
        return userProfileRepository.findBySessionId(sessionId)
                .orElse(null);
    }
}

