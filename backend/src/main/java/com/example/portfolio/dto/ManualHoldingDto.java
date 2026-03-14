package com.example.portfolio.dto;

import com.example.portfolio.model.FundCategory;
import lombok.Data;

@Data
public class ManualHoldingDto {
    private String fundName;
    private String folioNumber;
    private FundCategory category;  // EQUITY, DEBT, HYBRID, OTHER

    private Double units;
    private Double currentNav;
    private Double currentValue;     // current market value in ₹
    private Double investedAmount;  // cost / invested in ₹
}
