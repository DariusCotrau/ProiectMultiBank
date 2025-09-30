package com.multibank.service;

import com.multibank.domain.BankAccount;
import com.multibank.domain.Transaction;
import com.multibank.integration.BankApiClient;
import com.multibank.integration.ExternalBankAccount;
import com.multibank.integration.ExternalTransaction;
import com.multibank.repository.BankAccountRepository;
import com.multibank.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BankSynchronizationService {

    private final List<BankApiClient> bankClients;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;

    public BankSynchronizationService(List<BankApiClient> bankClients,
                                      BankAccountRepository bankAccountRepository,
                                      TransactionRepository transactionRepository) {
        this.bankClients = bankClients;
        this.bankAccountRepository = bankAccountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void synchronizeAll() {
        for (BankApiClient bankClient : bankClients) {
            synchronizeBank(bankClient);
        }
    }

    private void synchronizeBank(BankApiClient bankClient) {
        List<ExternalBankAccount> accounts = bankClient.fetchAccounts();
        Map<String, BankAccount> syncedAccounts = new HashMap<>();
        for (ExternalBankAccount external : accounts) {
            BankAccount account = bankAccountRepository.findByExternalId(external.getId())
                    .orElseGet(() -> new BankAccount(external.getId(), external.getBankName(), external.getAccountNumber(),
                            external.getIban(), external.getCurrency(), external.getBalance(), external.getType()));
            account.setBankName(external.getBankName());
            account.setAccountNumber(external.getAccountNumber());
            account.setIban(external.getIban());
            account.setCurrency(external.getCurrency());
            account.setBalance(external.getBalance());
            account.setType(external.getType());
            bankAccountRepository.save(account);
            syncedAccounts.put(external.getId(), account);
        }

        for (ExternalBankAccount external : accounts) {
            BankAccount localAccount = syncedAccounts.get(external.getId());
            if (localAccount == null) {
                continue;
            }
            for (ExternalTransaction externalTransaction : bankClient.fetchTransactions(external.getId())) {
                Transaction transaction = transactionRepository.findByExternalId(externalTransaction.getId())
                        .orElseGet(Transaction::new);
                transaction.setExternalId(externalTransaction.getId());
                transaction.setBankAccount(localAccount);
                transaction.setBookingDate(externalTransaction.getBookingDate());
                transaction.setAmount(normalizeAmount(externalTransaction.getAmount()));
                transaction.setCurrency(externalTransaction.getCurrency());
                transaction.setCategory(externalTransaction.getCategory());
                transaction.setDescription(externalTransaction.getDescription());
                transaction.setMerchant(externalTransaction.getMerchant());
                transaction.setDirection(externalTransaction.getDirection());
                transactionRepository.save(transaction);
            }
        }
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.abs();
    }
}
