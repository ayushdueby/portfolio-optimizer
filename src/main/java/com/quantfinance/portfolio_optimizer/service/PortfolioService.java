package com.quantfinance.portfolio_optimizer.service;

import com.quantfinance.portfolio_optimizer.dto.PortfolioCreateRequest;
import com.quantfinance.portfolio_optimizer.model.Portfolio;
import com.quantfinance.portfolio_optimizer.model.Position;
import com.quantfinance.portfolio_optimizer.repository.PortfolioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;

    public PortfolioService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    public Portfolio createPortfolio(PortfolioCreateRequest request) {
        Portfolio portfolio = new Portfolio();
        portfolio.setName(request.getName());
        portfolio.setTotalCapital(request.getCapital());
        portfolio.setDescription(request.getDescription());
        portfolio.setTimeStamp(LocalDateTime.now().toString());
        portfolio.setPositionList(new ArrayList<>());
        return portfolioRepository.save(portfolio);
    }

    public Portfolio getPortfolioById(String id) {
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
    }

    public List<Portfolio> getAllPortfolio() {
        return portfolioRepository.findAll();
    }

    public Portfolio updatePortfolio(String id, PortfolioCreateRequest request) {
        Portfolio portfolio = getPortfolioById(id);
        portfolio.setName(request.getName());
        portfolio.setTotalCapital(request.getCapital());
        portfolio.setDescription(request.getDescription());
        portfolio.setTimeStamp(LocalDateTime.now().toString());
        return portfolioRepository.save(portfolio);
    }

    public void deletePortfolio(String id) {
        portfolioRepository.deleteById(id);
    }

    public Portfolio addPositionToPortfolio(String portfolioId, Position position) {
        Portfolio portfolio = getPortfolioById(portfolioId);
        portfolio.getPositionList().add(position);
        return portfolioRepository.save(portfolio);
    }
}