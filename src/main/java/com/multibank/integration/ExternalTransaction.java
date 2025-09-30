package com.multibank.integration;

import com.multibank.domain.TransactionCategory;
import com.multibank.domain.TransactionDirection;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExternalTransaction {
    private String id;
    private String accountId;
    private LocalDate bookingDate;
    private BigDecimal amount;
    private String currency;
    private TransactionCategory category;
    private String description;
    private String merchant;
    private TransactionDirection direction;

    public ExternalTransaction() {
    }

    public ExternalTransaction(String id, String accountId, LocalDate bookingDate, BigDecimal amount, String currency,
                               TransactionCategory category, String description, String merchant,
                               TransactionDirection direction) {
        this.id = id;
        this.accountId = accountId;
        this.bookingDate = bookingDate;
        this.amount = amount;
        this.currency = currency;
        this.category = category;
        this.description = description;
        this.merchant = merchant;
        this.direction = direction;
    }

    public String getId() {
        return id;
    }

    public String getAccountId() {
        return accountId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public TransactionCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getMerchant() {
        return merchant;
    }

    public TransactionDirection getDirection() {
        return direction;
    }
}
