package com.example.portfolio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "portfolios")
public class Portfolio {

    @Id
    private String id;

    private String sessionId;

    private List<MutualFundHolding> holdings;

    private Double totalInvestedAmount;
    private Double currentPortfolioValue;
    private Double overallGainLossAmount;
    private Double overallGainLossPercent;
    private Double overallXirr;

    private Map<String, Double> assetAllocation; // e.g. Equity, Debt, Hybrid, Others
    private Map<String, Double> categoryBreakdown; // e.g. Large Cap, Mid Cap, etc.
}

