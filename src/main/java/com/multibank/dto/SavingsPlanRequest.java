package com.multibank.dto;

import com.multibank.domain.TransactionCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SavingsPlanRequest {

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal targetAmount;

    @NotNull
    @FutureOrPresent
    private LocalDate targetDate;

    private TransactionCategory focusCategory;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public TransactionCategory getFocusCategory() {
        return focusCategory;
    }

    public void setFocusCategory(TransactionCategory focusCategory) {
        this.focusCategory = focusCategory;
    }
}
