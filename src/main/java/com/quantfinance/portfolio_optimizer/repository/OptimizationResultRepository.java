package com.quantfinance.portfolio_optimizer.repository;

import com.quantfinance.portfolio_optimizer.model.OptimizationResult;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OptimizationResultRepository extends MongoRepository<OptimizationResult, String> {
    List<OptimizationResult> findByPortfolioId(String portfolioId);
}