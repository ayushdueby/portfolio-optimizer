package com.quantfinance.portfolio_optimizer.model;

import lombok.Data;

import java.util.List;

@Data
public class Portfolio {
    private String Id;
    private String name;
    private Integer totalCapital;
    private String description;
    private String timeStamp;
    private List<Position> positionList;
}
