package com.multibank.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping
    public Map<String, Object> index() {
        return Map.of(
                "message", "MultiBank API is running.",
                "ui", "/",
                "availableEndpoints", List.of(
                        "POST /api/banks/sync",
                        "GET /api/banks/accounts",
                        "GET /api/transactions",
                        "GET /api/analytics/monthly-spending",
                        "GET /api/analytics/category-totals",
                        "GET /api/savings",
                        "POST /api/savings",
                        "PUT /api/savings/{id}",
                        "POST /api/savings/{id}/contribute",
                        "DELETE /api/savings/{id}",
                        "POST /api/qr"
                )
        );
    }
}
