package com.quantfinance.portfolio_optimizer.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "assets")
@Data
public class Asset {
    @Id
    private String id;
    private String ticker;
    private String name;
    private List<Double> historicalReturns;
    private Double expectedReturn;
    private Double volatility;
}