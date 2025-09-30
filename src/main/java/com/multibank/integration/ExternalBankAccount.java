package com.multibank.integration;

import java.math.BigDecimal;

public class ExternalBankAccount {
    private String id;
    private String bankName;
    private String accountNumber;
    private String iban;
    private String currency;
    private BigDecimal balance;
    private String type;

    public ExternalBankAccount() {
    }

    public ExternalBankAccount(String id, String bankName, String accountNumber, String iban, String currency,
                               BigDecimal balance, String type) {
        this.id = id;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.iban = iban;
        this.currency = currency;
        this.balance = balance;
        this.type = type;
    }

    public String getId() {
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
