package com.multibank.integration;

import com.multibank.config.BankIntegrationProperties;
import com.multibank.domain.TransactionCategory;
import com.multibank.domain.TransactionDirection;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class BcrBankClient extends AbstractRestBankClient {

    public BcrBankClient(RestTemplate restTemplate, BankIntegrationProperties properties) {
        super(restTemplate, properties.getBcr());
    }

    @Override
    public String getBankName() {
        return "BCR";
    }

    @Override
    protected List<ExternalBankAccount> stubAccounts() {
        return List.of(
                new ExternalBankAccount("bcr-1", getBankName(), "RO49RNCB0099012345678901", "RO49RNCB0099012345678901",
                        "RON", new BigDecimal("12345.67"), "Current"),
                new ExternalBankAccount("bcr-2", getBankName(), "DE12500105170648489890", "RO59RNCB0099012345678902",
                        "EUR", new BigDecimal("2560.00"), "Savings")
        );
    }

    @Override
    protected List<ExternalTransaction> stubTransactions(String accountId) {
        return List.of(
                new ExternalTransaction("bcr-trx-1", accountId, LocalDate.now().minusDays(2), new BigDecimal("-150.45"),
                        "RON", TransactionCategory.GROCERY, "Mega Image", "Mega Image", TransactionDirection.OUTFLOW),
                new ExternalTransaction("bcr-trx-2", accountId, LocalDate.now().minusDays(10), new BigDecimal("4500.00"),
                        "RON", TransactionCategory.INCOME, "Salariu", "Company SRL", TransactionDirection.INFLOW)
        );
    }
}
