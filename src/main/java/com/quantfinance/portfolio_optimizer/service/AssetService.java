package com.quantfinance.portfolio_optimizer.service;

import com.quantfinance.portfolio_optimizer.repository.AssetRepository;
import org.springframework.stereotype.Service;

@Service
public class AssetService {
    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }
}