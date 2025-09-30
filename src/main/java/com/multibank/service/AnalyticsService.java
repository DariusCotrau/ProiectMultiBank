package com.multibank.service;

import com.multibank.domain.Transaction;
import com.multibank.domain.TransactionCategory;
import com.multibank.domain.TransactionDirection;
import com.multibank.dto.SpendingChartPoint;
import com.multibank.dto.TransactionFilterRequest;
import com.multibank.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final TransactionRepository transactionRepository;

    public AnalyticsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<SpendingChartPoint> getMonthlySpending(TransactionFilterRequest filter) {
        List<Transaction> transactions = transactionRepository.search(
                filter.getAccountId(),
                filter.getCategory(),
                filter.getStartDate(),
                filter.getEndDate()
        );

        Map<YearMonth, BigDecimal> grouped = transactions.stream()
                .filter(transaction -> transaction.getDirection() == TransactionDirection.OUTFLOW)
                .collect(Collectors.groupingBy(
                        transaction -> YearMonth.from(transaction.getBookingDate()),
                        TreeMap::new,
                        Collectors.mapping(Transaction::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        return grouped.entrySet().stream()
                .map(entry -> new SpendingChartPoint(entry.getKey(), entry.getValue().setScale(2, RoundingMode.HALF_UP)))
                .sorted(Comparator.comparing(SpendingChartPoint::getPeriod))
                .collect(Collectors.toList());
    }

    public Map<TransactionCategory, BigDecimal> getCategoryTotals(TransactionFilterRequest filter) {
        List<Transaction> transactions = transactionRepository.search(
                filter.getAccountId(),
                filter.getCategory(),
                filter.getStartDate(),
                filter.getEndDate()
        );

        return transactions.stream()
                .filter(transaction -> transaction.getDirection() == TransactionDirection.OUTFLOW)
                .filter(transaction -> transaction.getCategory() != null)
                .collect(Collectors.groupingBy(Transaction::getCategory,
                        Collectors.mapping(Transaction::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
    }
}
