package com.example.portfolio.service;

import com.example.portfolio.model.Portfolio;
import com.example.portfolio.model.Suggestion;
import com.example.portfolio.model.UserProfile;
import com.example.portfolio.repository.SuggestionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ClaudeSuggestionService {

    private final SuggestionRepository suggestionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.api.url}")
    private String apiUrl;

    public Suggestion analysePortfolio(UserProfile profile, Portfolio portfolio) throws IOException {
        Suggestion existing = suggestionRepository.findBySessionId(profile.getSessionId()).orElse(null);
        if (existing != null) {
            return existing;
        }

        String prompt = buildPrompt(profile, portfolio);
        String escapedPrompt = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");

        OkHttpClient client = new OkHttpClient();
        MediaType jsonMediaType = MediaType.parse("application/json");

        String requestBodyJson = """
                {
                  "model": "claude-3-5-sonnet-20241022",
                  "max_tokens": 1200,
                  "messages": [
                    {
                      "role": "user",
                      "content": [
                        { "type": "text", "text": "%s" }
                      ]
                    }
                  ]
                }
                """.formatted(escapedPrompt);

        RequestBody body = RequestBody.create(requestBodyJson, jsonMediaType);

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Claude API error: " + response.code() + " " + response.message());
            }
            String responseBody = response.body() != null ? response.body().string() : "";
            return parseAndSaveSuggestion(profile.getSessionId(), responseBody);
        }
    }

    private String buildPrompt(UserProfile profile, Portfolio portfolio) throws JsonProcessingException {
        return """
                You are an expert Indian mutual fund advisor and financial planner.
                The user profile and portfolio data are below. Analyse them and respond in clearly labelled sections:
                1) Portfolio Summary
                2) Risk Analysis
                3) Rebalancing Advice
                4) Better Fund Alternatives
                5) Tax Optimisation
                6) Goal-based Planning

                Always assume amounts are in Indian Rupees (₹). Be specific, concise, and practical.

                USER PROFILE:
                Name: %s
                Age: %s
                Monthly Income: %s
                Monthly Investment: %s
                Risk Appetite: %s

                PORTFOLIO SUMMARY:
                Total Invested: %s
                Current Value: %s
                Overall Gain/Loss Amount: %s
                Overall Gain/Loss %%: %s
                Overall XIRR: %s

                HOLDINGS:
                %s

                Format your answer with headings matching the six sections above.
                """.formatted(
                profile.getName(),
                profile.getAge(),
                profile.getMonthlyIncome(),
                profile.getMonthlyInvestment(),
                profile.getRiskAppetite(),
                portfolio.getTotalInvestedAmount(),
                portfolio.getCurrentPortfolioValue(),
                portfolio.getOverallGainLossAmount(),
                portfolio.getOverallGainLossPercent(),
                portfolio.getOverallXirr(),
                portfolio.getHoldings() != null ? objectMapper.writeValueAsString(portfolio.getHoldings()) : "[]"
        );
    }

    private Suggestion parseAndSaveSuggestion(String sessionId, String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        StringBuilder fullText = new StringBuilder();
        if (root.has("content") && root.get("content").isArray()) {
            for (JsonNode node : root.get("content")) {
                if (node.has("text")) {
                    fullText.append(node.get("text").asText());
                } else if (node.has("type") && "text".equals(node.get("type").asText()) && node.has("text")) {
                    fullText.append(node.get("text").asText());
                }
            }
        }

        String text = fullText.toString();

        Suggestion suggestion = Suggestion.builder()
                .sessionId(sessionId)
                .summary(text)
                .riskAnalysis("")
                .rebalancing("")
                .alternatives("")
                .taxTips("")
                .goalPlanning("")
                .build();

        return suggestionRepository.save(suggestion);
    }
}

