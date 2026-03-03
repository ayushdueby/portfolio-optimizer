package com.quantfinance.portfolio_optimizer.repository;

import com.quantfinance.portfolio_optimizer.model.Asset;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.util.Assert;

import java.util.Optional;


public interface AssetRepository extends MongoRepository<Asset,String> {
    Optional<Asset>findByTicker(String ticker);
}
