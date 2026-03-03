package com.quantfinance.portfolio_optimizer.controller;

import com.quantfinance.portfolio_optimizer.dto.PortfolioCreateRequest;
import com.quantfinance.portfolio_optimizer.dto.PortfolioResponse;
import com.quantfinance.portfolio_optimizer.model.Portfolio;
import com.quantfinance.portfolio_optimizer.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    @Autowired
    PortfolioService portfolioService;

    @PostMapping
    public ResponseEntity<PortfolioResponse> createPortfolio(@RequestBody PortfolioCreateRequest request) {
        Portfolio portfolio = portfolioService.createPortfolio(request);
        return ResponseEntity.ok(convertToResponse(portfolio));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PortfolioResponse> getPortfolioById(@PathVariable String id) {
        Portfolio portfolio = portfolioService.getPortfolioById(id);
        return ResponseEntity.ok(convertToResponse(portfolio));
    }

    @GetMapping
    public ResponseEntity<List<PortfolioResponse>> getAllPortfolios() {
        List<Portfolio> portfolios = portfolioService.getAllPortfolio();
        List<PortfolioResponse> responses = new ArrayList<>();

        for (Portfolio portfolio : portfolios) {
            responses.add(convertToResponse(portfolio));
        }
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PortfolioResponse> updatePortfolio(
            @PathVariable String id,
            @RequestBody PortfolioCreateRequest request) {
        Portfolio updated = portfolioService.updatePortfolio(id, request);
        return ResponseEntity.ok(convertToResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable String id) {
        portfolioService.deletePortfolio(id);
        return ResponseEntity.noContent().build();
    }

    private PortfolioResponse convertToResponse(Portfolio portfolio) {
        PortfolioResponse response = new PortfolioResponse();
        response.setId(portfolio.getId());
        response.setName(portfolio.getName());
        response.setTotalCapital(portfolio.getTotalCapital());
        response.setDescription(portfolio.getDescription());
        response.setTimeStamp(portfolio.getTimeStamp());
        response.setPositionList(portfolio.getPositionList());
        return response;
    }
}