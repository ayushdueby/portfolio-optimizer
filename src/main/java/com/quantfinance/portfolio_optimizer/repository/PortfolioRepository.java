package com.quantfinance.portfolio_optimizer.repository;

import com.quantfinance.portfolio_optimizer.model.Portfolio;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

public interface PortfolioRepository extends MongoRepository<Portfolio,String> {
}
