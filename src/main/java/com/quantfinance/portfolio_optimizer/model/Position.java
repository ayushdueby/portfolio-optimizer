package com.quantfinance.portfolio_optimizer.model;

import lombok.Data;

@Data
public class Position {
    private String ticker;
    private Double quantity;
    private Double currentPrice;
    private Double weight;
}