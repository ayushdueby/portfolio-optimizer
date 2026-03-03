package com.quantfinance.portfolio_optimizer.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "portfolios")
@Data
public class Portfolio {
    @Id
    private String id;
    private String name;
    private Integer totalCapital;
    private String description;
    private String timeStamp;
    private List<Position> positionList;
}