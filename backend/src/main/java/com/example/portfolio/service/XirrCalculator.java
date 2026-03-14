package com.example.portfolio.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class XirrCalculator {

    public static double calculateXirr(List<Double> cashFlows, List<LocalDate> dates) {
        if (cashFlows.size() != dates.size() || cashFlows.isEmpty()) {
            throw new IllegalArgumentException("Cash flows and dates must be same size and non-empty");
        }
        double guess = 0.1;
        double tolerance = 1e-6;
        int maxIterations = 100;

        for (int i = 0; i < maxIterations; i++) {
            double f = npv(cashFlows, dates, guess);
            double fPrime = derivative(cashFlows, dates, guess);
            if (Math.abs(fPrime) < 1e-10) {
                break;
            }
            double newGuess = guess - f / fPrime;
            if (Math.abs(newGuess - guess) <= tolerance) {
                return newGuess * 100.0;
            }
            guess = newGuess;
        }

        return guess * 100.0;
    }

    private static double npv(List<Double> cashFlows, List<LocalDate> dates, double rate) {
        LocalDate start = dates.get(0);
        double npv = 0.0;
        for (int i = 0; i < cashFlows.size(); i++) {
            long days = ChronoUnit.DAYS.between(start, dates.get(i));
            double yearFraction = days / 365.0;
            npv += cashFlows.get(i) / Math.pow(1.0 + rate, yearFraction);
        }
        return npv;
    }

    private static double derivative(List<Double> cashFlows, List<LocalDate> dates, double rate) {
        LocalDate start = dates.get(0);
        double derivative = 0.0;
        for (int i = 0; i < cashFlows.size(); i++) {
            long days = ChronoUnit.DAYS.between(start, dates.get(i));
            double yearFraction = days / 365.0;
            derivative += -yearFraction * cashFlows.get(i) /
                    Math.pow(1.0 + rate, yearFraction + 1.0);
        }
        return derivative;
    }
}

