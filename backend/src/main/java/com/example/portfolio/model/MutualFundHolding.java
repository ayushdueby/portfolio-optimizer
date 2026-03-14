package com.example.portfolio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MutualFundHolding {

    private String fundName;
    private String folioNumber;
    private FundCategory category;

    private Double units;
    private Double purchaseNav;
    private LocalDate purchaseDate;

    private Double currentNav;
    private Double currentValue;

    private Double investedAmount;
    private Double gainLossAmount;
    private Double gainLossPercent;
    private Double xirr;
}

