package com.example.portfolio.controller;

import com.example.portfolio.model.Portfolio;
import com.example.portfolio.model.Suggestion;
import com.example.portfolio.model.UserProfile;
import com.example.portfolio.repository.PortfolioRepository;
import com.example.portfolio.repository.UserProfileRepository;
import com.example.portfolio.service.ClaudeSuggestionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/suggestions")
@RequiredArgsConstructor
public class SuggestionController {

    private final UserProfileRepository userProfileRepository;
    private final PortfolioRepository portfolioRepository;
    private final ClaudeSuggestionService claudeSuggestionService;

    @PostMapping("/analyse")
    public ResponseEntity<Suggestion> analyse(@RequestBody AnalyseRequest request) throws IOException {
        UserProfile profile = userProfileRepository.findBySessionId(request.getSessionId())
                .orElse(null);
        Portfolio portfolio = portfolioRepository.findBySessionId(request.getSessionId())
                .orElse(null);

        if (profile == null || portfolio == null) {
            return ResponseEntity.badRequest().build();
        }

        Suggestion suggestion = claudeSuggestionService.analysePortfolio(profile, portfolio);
        return ResponseEntity.ok(suggestion);
    }

    @Data
    public static class AnalyseRequest {
        private String sessionId;
    }
}

