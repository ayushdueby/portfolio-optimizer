package com.example.portfolio.controller;

import com.example.portfolio.dto.ManualHoldingDto;
import com.example.portfolio.model.Portfolio;
import com.example.portfolio.service.PortfolioService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping(value = "/upload-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPdf(
            @RequestParam("sessionId") String sessionId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "password", required = false) String password
    ) {
        if (sessionId == null || sessionId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Portfolio portfolio = portfolioService.uploadPdf(sessionId.trim(), file, password);
            return ResponseEntity.ok(portfolio);
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "PDF read failed."));
        }
    }

    @PostMapping("/fetch-cams")
    public ResponseEntity<Portfolio> fetchCams(@RequestBody FetchCamsRequest request) {
        Portfolio portfolio = portfolioService.fetchFromCams(request.getSessionId(), request.getPan());
        return ResponseEntity.ok(portfolio);
    }

    @PostMapping("/manual")
    public ResponseEntity<Portfolio> importManual(
            @RequestParam("sessionId") String sessionId,
            @RequestBody List<ManualHoldingDto> holdings
    ) {
        if (sessionId == null || sessionId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Portfolio portfolio = portfolioService.importManualHoldings(sessionId.trim(), holdings != null ? holdings : List.of());
        return ResponseEntity.ok(portfolio);
    }

    @PostMapping(value = "/upload-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadCsv(
            @RequestParam("sessionId") String sessionId,
            @RequestPart("file") MultipartFile file
    ) {
        if (sessionId == null || sessionId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Portfolio portfolio = portfolioService.uploadCsv(sessionId.trim(), file);
            return ResponseEntity.ok(portfolio);
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "CSV read failed."));
        }
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<Portfolio> getPortfolio(@PathVariable String sessionId) {
        Portfolio portfolio = portfolioService.getBySessionId(sessionId);
        if (portfolio == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(portfolio);
    }

    @Data
    public static class FetchCamsRequest {
        private String pan;
        private String sessionId;
    }
}

