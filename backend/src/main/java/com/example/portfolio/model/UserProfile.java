package com.example.portfolio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class UserProfile {

    @Id
    private String id;

    private String sessionId;

    private String name;

    private Integer age;

    private Double monthlyIncome;

    private Double monthlyInvestment;

    private RiskAppetite riskAppetite;
}

