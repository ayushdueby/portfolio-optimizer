package com.example.portfolio.service;

import com.example.portfolio.model.MutualFundHolding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ExternalPortfolioClient {

    @Value("${cams.api.base-url}")
    private String camsBaseUrl;

    @Value("${kfintech.api.base-url}")
    private String kfintechBaseUrl;

    public List<MutualFundHolding> fetchFromCamsOrKfintech(String pan, String sessionId) {
        // Placeholder: integrate with actual CAMS/KFintech APIs
        // For now, return an empty list so the rest of the flow works.
        return Collections.emptyList();
    }
}

