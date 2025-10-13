package com.multibank.integration;

import com.multibank.config.BankIntegrationProperties;
import com.multibank.domain.TransactionCategory;
import com.multibank.domain.TransactionDirection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BtBankClient extends AbstractRestBankClient {

    public BtBankClient(BankIntegrationProperties properties) {
        super(properties.getBt());
    }

    @Override
    public String getBankName() {
        return "BT";
    }

    @Override
    protected List<ExternalBankAccount> stubAccounts() {
        return List.of(
                new ExternalBankAccount("bt-1", getBankName(), "RO09BTRL0000123456789012", "RO09BTRL0000123456789012",
                        "RON", new BigDecimal("9500.00"), "Current"),
                new ExternalBankAccount("bt-2", getBankName(), "RO14BTRL0000123456789013", "RO14BTRL0000123456789013",
                        "RON", new BigDecimal("4200.50"), "Credit")
        );
    }

    @Override
    protected List<ExternalTransaction> stubTransactions(String accountId) {
        return List.of(
                new ExternalTransaction("bt-trx-1", accountId, LocalDate.now().minusDays(4), new BigDecimal("-75.00"),
                        "RON", TransactionCategory.TRANSPORT, "Uber", "Uber BV", TransactionDirection.OUTFLOW),
                new ExternalTransaction("bt-trx-2", accountId, LocalDate.now().minusDays(1), new BigDecimal("-220.00"),
                        "RON", TransactionCategory.ENTERTAINMENT, "Cinema", "Cinema City", TransactionDirection.OUTFLOW)
        );
    }
}
