package com.multibank.dto;

import java.math.BigDecimal;

public class BankAccountResponse {
    private Long id;
    private String bankName;
    private String accountNumber;
    private String iban;
    private String currency;
    private BigDecimal balance;
    private String type;

    public BankAccountResponse() {
    }

    public BankAccountResponse(Long id, String bankName, String accountNumber, String iban, String currency,
                               BigDecimal balance, String type) {
        this.id = id;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.iban = iban;
        this.currency = currency;
        this.balance = balance;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getIban() {
        return iban;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getType() {
        return type;
    }
}
