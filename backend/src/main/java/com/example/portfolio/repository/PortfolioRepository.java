package com.example.portfolio.repository;

import com.example.portfolio.model.Portfolio;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PortfolioRepository extends MongoRepository<Portfolio, String> {

    Optional<Portfolio> findBySessionId(String sessionId);
}

