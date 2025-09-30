package com.multibank.controller;

import com.multibank.dto.ContributionRequest;
import com.multibank.dto.SavingsPlanRequest;
import com.multibank.dto.SavingsPlanResponse;
import com.multibank.service.SavingsPlanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/savings")
public class SavingsPlanController {

    private final SavingsPlanService savingsPlanService;

    public SavingsPlanController(SavingsPlanService savingsPlanService) {
        this.savingsPlanService = savingsPlanService;
    }

    @PostMapping
    public ResponseEntity<SavingsPlanResponse> createPlan(@Valid @RequestBody SavingsPlanRequest request) {
        return ResponseEntity.ok(savingsPlanService.createPlan(request));
    }

    @GetMapping
    public ResponseEntity<List<SavingsPlanResponse>> getPlans() {
        return ResponseEntity.ok(savingsPlanService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsPlanResponse> updatePlan(@PathVariable Long id,
                                                           @Valid @RequestBody SavingsPlanRequest request) {
        return ResponseEntity.ok(savingsPlanService.updatePlan(id, request));
    }

    @PostMapping("/{id}/contribute")
    public ResponseEntity<SavingsPlanResponse> contribute(@PathVariable Long id,
                                                           @Valid @RequestBody ContributionRequest contributionRequest) {
        return ResponseEntity.ok(savingsPlanService.contribute(id, contributionRequest.getAmount()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        savingsPlanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
