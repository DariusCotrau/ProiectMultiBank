package com.multibank.repository;

import com.multibank.domain.SavingsPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingsPlanRepository extends JpaRepository<SavingsPlan, Long> {
}
