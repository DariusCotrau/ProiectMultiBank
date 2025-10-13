package com.multibank.repository;

import com.multibank.domain.SavingsPlan;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class SavingsPlanRepository {

    private final EntityManagerFactory emf;

    public SavingsPlanRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public List<SavingsPlan> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<SavingsPlan> q = em.createQuery("select s from SavingsPlan s", SavingsPlan.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<SavingsPlan> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            SavingsPlan plan = em.find(SavingsPlan.class, id);
            return Optional.ofNullable(plan);
        } finally {
            em.close();
        }
    }

    public SavingsPlan save(SavingsPlan entity) {
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

    public void deleteById(Long id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SavingsPlan plan = em.find(SavingsPlan.class, id);
            if (plan != null) {
                em.remove(plan);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
