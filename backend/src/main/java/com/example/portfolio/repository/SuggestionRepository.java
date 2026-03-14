package com.example.portfolio.repository;

import com.example.portfolio.model.Suggestion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SuggestionRepository extends MongoRepository<Suggestion, String> {

    Optional<Suggestion> findBySessionId(String sessionId);
}

