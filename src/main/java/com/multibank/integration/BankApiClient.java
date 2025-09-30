package com.multibank.integration;

import java.util.List;

public interface BankApiClient {

    List<ExternalBankAccount> fetchAccounts();

    List<ExternalTransaction> fetchTransactions(String accountId);

    String getBankName();
}
