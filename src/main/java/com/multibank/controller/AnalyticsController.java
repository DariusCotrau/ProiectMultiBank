package com.multibank.controller;

import com.multibank.domain.TransactionCategory;
import com.multibank.dto.SpendingChartPoint;
import com.multibank.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/monthly-spending")
    public ResponseEntity<List<SpendingChartPoint>> getMonthlySpending(
            @RequestParam(value = "accountId", required = false) Long accountId,
            @RequestParam(value = "category", required = false) TransactionCategory category,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(analyticsService.getMonthlySpending(accountId, category, startDate, endDate));
    }

    @GetMapping("/category-totals")
    public ResponseEntity<Map<TransactionCategory, BigDecimal>> getCategoryTotals(
            @RequestParam(value = "accountId", required = false) Long accountId,
            @RequestParam(value = "category", required = false) TransactionCategory category,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(analyticsService.getCategoryTotals(accountId, category, startDate, endDate));
    }

}
