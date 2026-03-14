package com.example.portfolio.service;

import com.example.portfolio.dto.ManualHoldingDto;
import com.example.portfolio.model.FundCategory;
import com.example.portfolio.model.MutualFundHolding;
import com.example.portfolio.model.Portfolio;
import com.example.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PdfPortfolioParser pdfPortfolioParser;
    private final ExternalPortfolioClient externalPortfolioClient;

    public Portfolio uploadPdf(String sessionId, MultipartFile file) throws IOException {
        return uploadPdf(sessionId, file, null);
    }

    public Portfolio uploadPdf(String sessionId, MultipartFile file, String password) throws IOException {
        List<MutualFundHolding> holdings = pdfPortfolioParser.parsePortfolio(file, password);
        return saveOrUpdatePortfolio(sessionId, holdings);
    }

    public Portfolio fetchFromCams(String sessionId, String pan) {
        List<MutualFundHolding> holdings = externalPortfolioClient.fetchFromCamsOrKfintech(pan, sessionId);
        return saveOrUpdatePortfolio(sessionId, holdings);
    }

    public Portfolio getBySessionId(String sessionId) {
        return portfolioRepository.findBySessionId(sessionId).orElse(null);
    }

    /**
     * Import holdings from manual entry (100% accurate – user-entered data).
     */
    public Portfolio importManualHoldings(String sessionId, List<ManualHoldingDto> dtos) {
        List<MutualFundHolding> holdings = new ArrayList<>();
        for (ManualHoldingDto d : dtos) {
            if (d.getFundName() == null || d.getFundName().isBlank()) continue;
            double currentVal = d.getCurrentValue() != null ? d.getCurrentValue() : 0.0;
            double invested = d.getInvestedAmount() != null ? d.getInvestedAmount() : currentVal;
            if (d.getUnits() != null && d.getCurrentNav() != null && currentVal == 0) {
                currentVal = d.getUnits() * d.getCurrentNav();
            }
            if (currentVal == 0 && d.getUnits() != null && d.getCurrentNav() != null) {
                currentVal = d.getUnits() * d.getCurrentNav();
            }
            double gainLoss = currentVal - invested;
            double gainPct = invested != 0 ? (gainLoss / invested) * 100.0 : 0.0;
            FundCategory cat = d.getCategory() != null ? d.getCategory() : FundCategory.OTHER;
            MutualFundHolding h = MutualFundHolding.builder()
                    .fundName(d.getFundName().trim())
                    .folioNumber(d.getFolioNumber())
                    .category(cat)
                    .units(d.getUnits())
                    .currentNav(d.getCurrentNav())
                    .currentValue(currentVal)
                    .investedAmount(invested)
                    .purchaseNav(d.getCurrentNav())
                    .gainLossAmount(gainLoss)
                    .gainLossPercent(gainPct)
                    .build();
            holdings.add(h);
        }
        return saveOrUpdatePortfolio(sessionId, holdings);
    }

    /**
     * Import holdings from CSV. Columns: fundName, units, currentNav, currentValue, investedAmount, folioNumber, category.
     * Header row optional; category = EQUITY|DEBT|HYBRID|OTHER.
     */
    public Portfolio uploadCsv(String sessionId, MultipartFile file) throws IOException {
        List<ManualHoldingDto> dtos = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int row = 0;
            while ((line = reader.readLine()) != null) {
                row++;
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = parseCsvLine(line);
                if (parts.length < 2) continue;
                if (row == 1 && parts[0].toLowerCase().contains("fund") && parts[0].toLowerCase().contains("name")) continue;
                ManualHoldingDto d = new ManualHoldingDto();
                d.setFundName(parts.length > 0 ? parts[0].trim() : "");
                if (parts.length >= 7) {
                    d.setUnits(parseDouble(parts[1]));
                    d.setCurrentNav(parseDouble(parts[2]));
                    d.setCurrentValue(parseDouble(parts[3]));
                    d.setInvestedAmount(parseDouble(parts[4]));
                    d.setFolioNumber(parts[5].trim().isEmpty() ? null : parts[5].trim());
                    try { d.setCategory(FundCategory.valueOf(parts[6].trim().toUpperCase())); } catch (Exception ignored) { }
                } else if (parts.length >= 2) {
                    d.setCurrentValue(parseDouble(parts[1]));
                    d.setInvestedAmount(d.getCurrentValue());
                }
                if (d.getFundName() != null && !d.getFundName().isEmpty() && (d.getCurrentValue() != null || (d.getUnits() != null && d.getCurrentNav() != null))) dtos.add(d);
            }
        }
        return importManualHoldings(sessionId, dtos);
    }

    private static String[] parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') inQuotes = !inQuotes;
            else if ((c == ',' && !inQuotes) || c == '\t') {
                out.add(cur.toString().replace("\"", "").trim());
                cur = new StringBuilder();
            } else cur.append(c);
        }
        out.add(cur.toString().replace("\"", "").trim());
        return out.toArray(new String[0]);
    }

    private static Double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(s.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Portfolio saveOrUpdatePortfolio(String sessionId, List<MutualFundHolding> holdings) {
        Map<String, Double> assetAllocation = new HashMap<>();
        Map<String, Double> categoryBreakdown = new HashMap<>();

        DoubleSummaryStatistics investedStats = holdings.stream()
                .mapToDouble(h -> h.getInvestedAmount() != null ? h.getInvestedAmount() : 0.0)
                .summaryStatistics();

        DoubleSummaryStatistics currentStats = holdings.stream()
                .mapToDouble(h -> h.getCurrentValue() != null ? h.getCurrentValue() : 0.0)
                .summaryStatistics();

        double totalInvested = investedStats.getSum();
        double currentValue = currentStats.getSum();
        double gainLossAmount = currentValue - totalInvested;
        double gainLossPercent = totalInvested != 0 ? (gainLossAmount / totalInvested) * 100.0 : 0.0;

        holdings.forEach(h -> {
            if (h.getCategory() != null) {
                String cat = h.getCategory().name();
                double value = h.getCurrentValue() != null ? h.getCurrentValue() : 0.0;
                assetAllocation.merge(cat, value, Double::sum);
                categoryBreakdown.merge(cat, value, Double::sum);
            }
        });

        double overallXirr = 0.0;

        Portfolio portfolio = portfolioRepository.findBySessionId(sessionId).orElseGet(Portfolio::new);
        portfolio.setSessionId(sessionId);
        portfolio.setHoldings(holdings);
        portfolio.setTotalInvestedAmount(totalInvested);
        portfolio.setCurrentPortfolioValue(currentValue);
        portfolio.setOverallGainLossAmount(gainLossAmount);
        portfolio.setOverallGainLossPercent(gainLossPercent);
        portfolio.setOverallXirr(overallXirr);
        portfolio.setAssetAllocation(assetAllocation);
        portfolio.setCategoryBreakdown(categoryBreakdown);

        return portfolioRepository.save(portfolio);
    }
}

