package com.example.portfolio.repository;

import com.example.portfolio.model.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserProfileRepository extends MongoRepository<UserProfile, String> {

    Optional<UserProfile> findBySessionId(String sessionId);
}

