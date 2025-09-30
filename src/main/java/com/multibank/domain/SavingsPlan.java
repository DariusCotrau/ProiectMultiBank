package com.multibank.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "savings_plans")
public class SavingsPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private BigDecimal targetAmount;

    private BigDecimal currentAmount;

    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    private TransactionCategory focusCategory;

    public SavingsPlan() {
    }

    public SavingsPlan(String name, BigDecimal targetAmount, BigDecimal currentAmount, LocalDate targetDate,
                        TransactionCategory focusCategory) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
        this.focusCategory = focusCategory;
    }

    public Long getId() {
        return id;
    }

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

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavingsPlan that = (SavingsPlan) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
