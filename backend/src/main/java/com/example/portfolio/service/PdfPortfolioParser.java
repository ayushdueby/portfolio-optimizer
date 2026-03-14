package com.example.portfolio.service;

import com.example.portfolio.model.FundCategory;
import com.example.portfolio.model.MutualFundHolding;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfPortfolioParser {

    private static final Logger log = LoggerFactory.getLogger(PdfPortfolioParser.class);

    private static final Pattern DECIMAL = Pattern.compile("[0-9][0-9,]*(?:\\.[0-9]+)?");
    private static final Pattern FOLIO = Pattern.compile("\\b[0-9]{8,}(?:\\s*[/-]\\s*[0-9]+)?\\b");

    // Lines that are headers, footers, or disclaimers - not scheme names (avoid "NIL" - matches NIPPON)
    private static final String[] BLACKLIST_PHRASES = {
            "Email Id", "As on", "Market Value", "Folio No", "ISIN", "Cost Value", "(INR)",
            "CAMS", "KFintech", "Please ensure", "GST", "Identification Number", "KYC",
            "Entry Load", "Exit Load", "redemption", "allotment", "registration date", "FIFO",
            "IDCW", "w.e.f.", "Not Applicable", "Loads and Fees", "friendly initiative",
            "consolidation", "check with your DP", "DEMAT holdings", "fundamental attributes",
            "Current Load Structure", "PEP status", "FATCA", "including SIP", "For subscriptions",
            "redeemed without", "Redemption of units", "changed to", "kindly update", "Bagh", "DELHI INDIA",
            "Direct Plan - Growth (Demat) :", "Growth Option (Demat) :", "Entry Load and Exit Load",
            "months from the date", "if redeemed", "switched-out", "No Exit Load"
    };

    public List<MutualFundHolding> parsePortfolio(MultipartFile file) throws IOException {
        return parsePortfolio(file, null);
    }

    public List<MutualFundHolding> parsePortfolio(MultipartFile file, String password) throws IOException {
        if (file == null || file.isEmpty()) return new ArrayList<>();
        boolean hasPassword = password != null && !password.isBlank();
        try (InputStream is = file.getInputStream()) {
            final PDDocument document = hasPassword ? PDDocument.load(is, password) : PDDocument.load(is);
            try {
                String text = new PDFTextStripper().getText(document);
                log.debug("PDF extracted {} characters", text != null ? text.length() : 0);
                List<MutualFundHolding> holdings = parseExtractedText(text);
                log.info("Parsed {} holdings from PDF", holdings.size());
                return holdings;
            } finally {
                document.close();
            }
        } catch (IOException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("decrypt") || msg.contains("password") || msg.contains("encrypted")) {
                throw new IOException(hasPassword
                        ? "PDF password is incorrect. Please check and try again."
                        : "PDF is password-protected. Please enter the statement password above and try again.", e);
            }
            log.warn("PDF parsing failed (returning empty holdings): {}", msg);
        } catch (Exception e) {
            log.warn("PDF parsing failed (returning empty holdings): {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    private boolean isBlacklisted(String line) {
        if (line == null || line.length() > 1000) return true;
        String lower = line.toLowerCase();
        for (String p : BLACKLIST_PHRASES) {
            if (lower.contains(p.toLowerCase())) return true;
        }
        if (line.contains(":") && (line.contains("th,") || line.contains("w.e.f") || line.contains("Nil"))) return true;
        return false;
    }

    private boolean isSchemeLine(String line) {
        if (line == null || line.length() < 10 || line.length() > 800) return false;
        String trimmed = line.trim();
        if (!trimmed.toLowerCase().contains("fund")) return false;
        if (!trimmed.matches("(?i).*(Direct|Growth|Demat|Plan).*")) return false;
        if (isBlacklisted(trimmed)) return false;
        return true;
    }

    /**
     * Extract fund name by stripping trailing numeric columns (folio, units, NAV, value).
     */
    private String extractFundName(String line) {
        // Remove trailing numbers (and trailing text that is just numbers/spaces)
        String name = line.replaceAll("\\s+", " ").trim();
        // Strip from the end: optional spaces + number + repeated (space + number)
        name = name.replaceAll("\\s+[0-9][0-9,]*(?:\\.[0-9]+)?(?:\\s+[0-9][0-9,]*(?:\\.[0-9]+)?)*\\s*$", "").trim();
        // If that left nothing useful, strip only the last few number-like tokens
        if (name.length() < 10) {
            name = line.replaceAll("\\s+", " ").trim();
            int i = name.length() - 1;
            while (i >= 0 && (Character.isDigit(name.charAt(i)) || name.charAt(i) == '.' || name.charAt(i) == ',' || name.charAt(i) == ' ')) i--;
            name = name.substring(0, i + 1).trim();
        }
        // Truncate at disclaimer (colon followed by disclaimer text)
        int colon = name.indexOf(" : ");
        if (colon > 20) name = name.substring(0, colon).trim();
        return name;
    }

    /**
     * Extract only numbers that could be units, NAV, or value. Exclude years and folio-like integers.
     */
    private List<Double> extractDataNumbers(String line) {
        List<Double> out = new ArrayList<>();
        Matcher m = DECIMAL.matcher(line);
        while (m.find()) {
            String s = m.group().replace(",", "");
            try {
                double v = Double.parseDouble(s);
                if (v <= 0) continue;
                if (v >= 2015 && v <= 2030 && Math.abs(v - Math.round(v)) < 1e-6) continue; // year
                if (v >= 1e7 && v < 1e12 && Math.abs(v - Math.round(v)) < 1e-2) continue;  // likely folio
                if (v >= 1e12) continue;
                out.add(v);
            } catch (NumberFormatException ignored) { }
        }
        return out;
    }

    /**
     * From list of data numbers (in line order), pick units, NAV, value.
     * CAMS table order often: ... Units, NAV, Market Value. Use last 2–3 numbers; fallback value = units * NAV.
     */
    private void assignUnitsNavValue(List<Double> numbers, double[] out) {
        out[0] = 0; out[1] = 0; out[2] = 0;
        if (numbers.size() < 2) return;
        int n = numbers.size();
        double b = numbers.get(n - 2);
        double c = numbers.get(n - 1);
        double a = n >= 3 ? numbers.get(n - 3) : b;
        double units, nav, value;
        if (n == 2) {
            double small = Math.min(b, c);
            double large = Math.max(b, c);
            if (small >= 0.01 && small <= 50_000 && large >= 1) {
                nav = small;
                value = large;
                units = value / nav;
            } else return;
        } else {
            if (c >= 1 && c <= 1e12) value = c;
            else value = (b >= 1 && b <= 1e12) ? b : a;
            if (value < 1 || value > 1e12) return;
            if (a >= 0.01 && a <= 50_000 && b >= 0.001 && b <= 1e9) {
                nav = a; units = b;
            } else if (b >= 0.01 && b <= 50_000 && a >= 0.001 && a <= 1e9) {
                units = a; nav = b;
            } else {
                nav = (b >= 0.01 && b <= 50_000) ? b : a;
                if (nav < 0.01 || nav > 50_000) return;
                units = value / nav;
            }
            if (Math.abs(value - units * nav) <= 0.5 * value + 1) {
                out[2] = value;
            } else {
                out[2] = units * nav;
            }
        }
        if (units < 0.0001 || units > 1e9) return;
        out[0] = units;
        out[1] = nav;
        if (out[2] == 0) out[2] = units * nav;
    }

    private List<MutualFundHolding> parseExtractedText(String text) {
        List<MutualFundHolding> holdings = new ArrayList<>();
        if (text == null || text.isBlank()) return holdings;

        String[] lines = text.split("\\r?\\n");
        int fundLineCount = 0;
        for (String line : lines) {
            if (line != null && line.toLowerCase().contains("fund")) fundLineCount++;
        }
        log.debug("Lines containing 'fund': {}", fundLineCount);

        List<String> toProcess = new ArrayList<>();
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            if (line.length() > 500) {
                for (String segment : line.split("\\s{2,}")) {
                    String s = segment.trim();
                    if (s.length() > 10) toProcess.add(s);
                }
            } else {
                toProcess.add(line);
            }
        }

        for (String line : toProcess) {
            if (!isSchemeLine(line)) continue;

            String name = extractFundName(line);
            if (name.length() < 10) continue;

            List<Double> dataNumbers = extractDataNumbers(line);
            if (dataNumbers.size() < 2) continue;

            double[] unv = new double[3];
            assignUnitsNavValue(dataNumbers, unv);
            if (unv[0] <= 0 || unv[1] <= 0 || unv[2] <= 0) continue;

            String folio = null;
            Matcher fm = FOLIO.matcher(line);
            if (fm.find()) folio = fm.group().replaceAll("\\s+", "");

            FundCategory category = inferCategory(name);
            double units = unv[0], nav = unv[1], value = unv[2];

            MutualFundHolding h = MutualFundHolding.builder()
                    .fundName(name)
                    .folioNumber(folio)
                    .category(category)
                    .units(units)
                    .currentNav(nav)
                    .currentValue(value)
                    .investedAmount(value)
                    .purchaseNav(nav)
                    .build();
            h.setGainLossAmount(0.0);
            h.setGainLossPercent(0.0);
            holdings.add(h);
        }
        return holdings;
    }

    private FundCategory inferCategory(String fundName) {
        String n = fundName.toLowerCase();
        if (n.contains("equity") || n.contains("elss") || n.contains("index") || n.contains("mid cap") || n.contains("small cap") || n.contains("flexi cap") || n.contains("multi cap")) return FundCategory.EQUITY;
        if (n.contains("debt") || n.contains("liquid") || n.contains("gilt") || n.contains("bond")) return FundCategory.DEBT;
        if (n.contains("hybrid") || n.contains("balanced")) return FundCategory.HYBRID;
        return FundCategory.OTHER;
    }
}
