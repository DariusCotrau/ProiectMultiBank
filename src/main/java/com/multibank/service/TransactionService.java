package com.multibank.service;

import com.multibank.domain.Transaction;
import com.multibank.domain.TransactionCategory;
import com.multibank.dto.TransactionResponse;
import com.multibank.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionResponse> searchTransactions(Long accountId,
                                                        TransactionCategory category,
                                                        LocalDate startDate,
                                                        LocalDate endDate) {
        List<Transaction> transactions = transactionRepository.search(
                accountId,
                category,
                startDate,
                endDate
        );

        return transactions.stream()
                .map(transaction -> new TransactionResponse(
                        transaction.getId(),
                        transaction.getBankAccount() != null ? transaction.getBankAccount().getId() : null,
                        transaction.getBookingDate(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getCategory(),
                        transaction.getDescription(),
                        transaction.getMerchant(),
                        transaction.getDirection()
                ))
                .collect(Collectors.toList());
    }
}
