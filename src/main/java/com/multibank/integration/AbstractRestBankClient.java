package com.multibank.integration;

import com.multibank.config.BankIntegrationProperties;

import java.util.List;

public abstract class AbstractRestBankClient implements BankApiClient {

    protected final BankIntegrationProperties.BankProperties properties;

    protected AbstractRestBankClient(BankIntegrationProperties.BankProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<ExternalBankAccount> fetchAccounts() {
        // Without Spring, we operate in stub/offline mode.
        // If real HTTP integration is needed, swap this with a Java 11 HttpClient implementation.
        return stubAccounts();
    }

    @Override
    public List<ExternalTransaction> fetchTransactions(String accountId) {
        // Stub/offline mode
        return stubTransactions(accountId);
    }

    protected abstract List<ExternalBankAccount> stubAccounts();

    protected abstract List<ExternalTransaction> stubTransactions(String accountId);
}
