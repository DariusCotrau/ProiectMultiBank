package com.multibank.repository;

import com.multibank.domain.Transaction;
import com.multibank.domain.TransactionCategory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TransactionRepository {

    private final EntityManagerFactory emf;

    public TransactionRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public List<Transaction> search(Long accountId,
                                    TransactionCategory category,
                                    LocalDate startDate,
                                    LocalDate endDate) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "select t from Transaction t where " +
                    "(:accountId is null or t.bankAccount.id = :accountId) and " +
                    "(:category is null or t.category = :category) and " +
                    "(:startDate is null or t.bookingDate >= :startDate) and " +
                    "(:endDate is null or t.bookingDate <= :endDate) order by t.bookingDate desc";
            TypedQuery<Transaction> q = em.createQuery(jpql, Transaction.class);
            q.setParameter("accountId", accountId);
            q.setParameter("category", category);
            q.setParameter("startDate", startDate);
            q.setParameter("endDate", endDate);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Transaction> findByExternalId(String externalId) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Transaction> q = em.createQuery("select t from Transaction t where t.externalId = :eid", Transaction.class);
            q.setParameter("eid", externalId);
            List<Transaction> res = q.getResultList();
            return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
        } finally {
            em.close();
        }
    }

    public Transaction save(Transaction entity) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (entity.getId() == null) {
                em.persist(entity);
            } else {
                entity = em.merge(entity);
            }
            tx.commit();
            return entity;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
