package com.multibank.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public class SpendingChartPoint {
    private YearMonth period;
    private BigDecimal totalAmount;

    public SpendingChartPoint() {
    }

    public SpendingChartPoint(YearMonth period, BigDecimal totalAmount) {
        this.period = period;
        this.totalAmount = totalAmount;
    }

    public YearMonth getPeriod() {
        return period;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
