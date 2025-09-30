package com.multibank.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String externalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private BankAccount bankAccount;

    private LocalDate bookingDate;

    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    private TransactionCategory category;

    private String description;

    private String merchant;

    @Enumerated(EnumType.STRING)
    private TransactionDirection direction;

    public Transaction() {
    }

    public Transaction(String externalId, BankAccount bankAccount, LocalDate bookingDate, BigDecimal amount, String currency,
                        TransactionCategory category, String description, String merchant, TransactionDirection direction) {
        this.externalId = externalId;
        this.bankAccount = bankAccount;
        this.bookingDate = bookingDate;
        this.amount = amount;
        this.currency = currency;
        this.category = category;
        this.description = description;
        this.merchant = merchant;
        this.direction = direction;
    }

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public TransactionCategory getCategory() {
        return category;
    }

    public void setCategory(TransactionCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public TransactionDirection getDirection() {
        return direction;
    }

    public void setDirection(TransactionDirection direction) {
        this.direction = direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
