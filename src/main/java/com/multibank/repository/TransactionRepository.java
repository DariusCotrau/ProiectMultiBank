package com.multibank.repository;

import com.multibank.domain.Transaction;
import com.multibank.domain.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("select t from Transaction t where (:accountId is null or t.bankAccount.id = :accountId) " +
            "and (:category is null or t.category = :category) " +
            "and (:startDate is null or t.bookingDate >= :startDate) " +
            "and (:endDate is null or t.bookingDate <= :endDate) order by t.bookingDate desc")
    List<Transaction> search(@Param("accountId") Long accountId,
                             @Param("category") TransactionCategory category,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate);

    Optional<Transaction> findByExternalId(String externalId);
}
