package com.davidhorobin.budgetbalance.repository;

import com.davidhorobin.budgetbalance.entity.Counterparty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CounterpartyRepo extends JpaRepository<Counterparty, Integer> {
    Optional<Counterparty> findByName(String name);
}
