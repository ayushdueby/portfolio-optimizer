package com.quantfinance.portfolio_optimizer.dto;

import lombok.Data;

@Data
public class PortfolioCreateRequest {
    private String name;
    private Integer capital;
    private String description;
}
