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
@Document(collection = "suggestions")
public class Suggestion {

    @Id
    private String id;

    private String sessionId;

    private String summary;
    private String riskAnalysis;
    private String rebalancing;
    private String alternatives;
    private String taxTips;
    private String goalPlanning;
}

