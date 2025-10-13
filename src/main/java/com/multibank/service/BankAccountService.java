package com.multibank.service;

import com.multibank.domain.BankAccount;
import com.multibank.dto.BankAccountResponse;
import com.multibank.repository.BankAccountRepository;

import java.util.List;
import java.util.stream.Collectors;

public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    public BankAccountService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    public List<BankAccountResponse> getAllAccounts() {
        return bankAccountRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BankAccountResponse mapToResponse(BankAccount account) {
        return new BankAccountResponse(account.getId(), account.getBankName(), account.getAccountNumber(),
                account.getIban(), account.getCurrency(), account.getBalance(), account.getType());
    }
}
