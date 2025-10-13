package com.multibank.service;

import com.multibank.domain.SavingsPlan;
import com.multibank.dto.SavingsPlanRequest;
import com.multibank.dto.SavingsPlanResponse;
import com.multibank.repository.SavingsPlanRepository;
import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

public class SavingsPlanService {

    private final SavingsPlanRepository savingsPlanRepository;

    public SavingsPlanService(SavingsPlanRepository savingsPlanRepository) {
        this.savingsPlanRepository = savingsPlanRepository;
    }

    public SavingsPlanResponse createPlan(SavingsPlanRequest request) {
        SavingsPlan plan = new SavingsPlan();
        plan.setName(request.getName());
        plan.setTargetAmount(request.getTargetAmount());
        plan.setCurrentAmount(BigDecimal.ZERO);
        plan.setTargetDate(request.getTargetDate());
        plan.setFocusCategory(request.getFocusCategory());
        savingsPlanRepository.save(plan);
        return map(plan);
    }

    public List<SavingsPlanResponse> findAll() {
        return savingsPlanRepository.findAll().stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    public SavingsPlanResponse updatePlan(Long id, SavingsPlanRequest request) {
        SavingsPlan plan = savingsPlanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Planul de economisire nu a fost gasit"));
        plan.setName(request.getName());
        plan.setTargetAmount(request.getTargetAmount());
        plan.setTargetDate(request.getTargetDate());
        plan.setFocusCategory(request.getFocusCategory());
        savingsPlanRepository.save(plan);
        return map(plan);
    }

    public SavingsPlanResponse contribute(Long id, BigDecimal amount) {
        SavingsPlan plan = savingsPlanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Planul de economisire nu a fost gasit"));
        BigDecimal current = plan.getCurrentAmount() == null ? BigDecimal.ZERO : plan.getCurrentAmount();
        plan.setCurrentAmount(current.add(amount));
        savingsPlanRepository.save(plan);
        return map(plan);
    }

    public void delete(Long id) {
        savingsPlanRepository.deleteById(id);
    }

    private SavingsPlanResponse map(SavingsPlan plan) {
        return new SavingsPlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getTargetAmount(),
                plan.getCurrentAmount(),
                plan.getTargetDate(),
                plan.getFocusCategory(),
                calculateProgress(plan)
        );
    }

    private double calculateProgress(SavingsPlan plan) {
        if (plan.getTargetAmount() == null || plan.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        BigDecimal current = plan.getCurrentAmount() == null ? BigDecimal.ZERO : plan.getCurrentAmount();
        return current
                .multiply(BigDecimal.valueOf(100))
                .divide(plan.getTargetAmount(), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
