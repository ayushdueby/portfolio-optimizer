package com.quantfinance.portfolio_optimizer.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "optimization_results")
@Data
public class OptimizationResult {
    @Id
    private String id;
    private String portfolioId;
    private String strategyType;
    private Map<String, Double> weights;
    private Double expectedReturn;
    private Double portfolioVolatility;
    private Double sharpeRatio;
    private LocalDateTime createdAt;
}