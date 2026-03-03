package com.quantfinance.portfolio_optimizer.dto;

import com.quantfinance.portfolio_optimizer.model.Position;
import lombok.Data;

import java.util.List;

@Data
public class PortfolioResponse {
    private String id;
    private String name;
    private Integer totalCapital;
    private String description;
    private String timeStamp;
    private List<Position> positionList;
}