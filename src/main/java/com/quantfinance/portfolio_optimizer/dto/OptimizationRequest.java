package com.quantfinance.portfolio_optimizer.dto;

import com.quantfinance.portfolio_optimizer.StrategyType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class OptimizationRequest {
    private String portfolioId;
    private List<String> tickers;
    private StrategyType strategyType;
    private Double targetReturn; //optional
    private Double maxRisk; //optional
}
