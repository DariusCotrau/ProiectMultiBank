package com.multibank.controller;

import com.multibank.domain.TransactionCategory;
import com.multibank.dto.SpendingChartPoint;
import com.multibank.dto.TransactionFilterRequest;
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
        TransactionFilterRequest filter = buildFilter(accountId, category, startDate, endDate);
        return ResponseEntity.ok(analyticsService.getMonthlySpending(filter));
    }

    @GetMapping("/category-totals")
    public ResponseEntity<Map<TransactionCategory, BigDecimal>> getCategoryTotals(
            @RequestParam(value = "accountId", required = false) Long accountId,
            @RequestParam(value = "category", required = false) TransactionCategory category,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        TransactionFilterRequest filter = buildFilter(accountId, category, startDate, endDate);
        return ResponseEntity.ok(analyticsService.getCategoryTotals(filter));
    }

    private TransactionFilterRequest buildFilter(Long accountId, TransactionCategory category, LocalDate startDate, LocalDate endDate) {
        TransactionFilterRequest filter = new TransactionFilterRequest();
        filter.setAccountId(accountId);
        filter.setCategory(category);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        return filter;
    }
}
