package com.multibank.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> index() {
        return Map.of(
                "message", "MultiBank API is running.",
                "availableEndpoints", List.of(
                        "POST /api/banks/sync",
                        "GET /api/banks/accounts",
                        "GET /api/transactions",
                        "GET /api/analytics/monthly-spending",
                        "GET /api/analytics/category-totals",
                        "POST /api/savings",
                        "POST /api/savings/{id}/contribute",
                        "POST /api/qr"
                )
        );
    }
}
