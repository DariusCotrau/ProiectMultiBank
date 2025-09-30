package com.multibank.dto;

import com.multibank.domain.TransactionCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SavingsPlanResponse {
    private Long id;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate targetDate;
    private TransactionCategory focusCategory;
    private double progress;

    public SavingsPlanResponse() {
    }

    public SavingsPlanResponse(Long id, String name, BigDecimal targetAmount, BigDecimal currentAmount,
                               LocalDate targetDate, TransactionCategory focusCategory, double progress) {
        this.id = id;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
        this.focusCategory = focusCategory;
        this.progress = progress;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public TransactionCategory getFocusCategory() {
        return focusCategory;
    }

    public double getProgress() {
        return progress;
    }
}
