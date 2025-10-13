package com.multibank.repository;

import com.multibank.domain.BankAccount;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class BankAccountRepository {

    private final EntityManagerFactory emf;

    public BankAccountRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public List<BankAccount> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<BankAccount> q = em.createQuery("select b from BankAccount b", BankAccount.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<BankAccount> findByExternalId(String externalId) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<BankAccount> q = em.createQuery("select b from BankAccount b where b.externalId = :eid", BankAccount.class);
            q.setParameter("eid", externalId);
            List<BankAccount> res = q.getResultList();
            return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
        } finally {
            em.close();
        }
    }

    public BankAccount save(BankAccount entity) {
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
