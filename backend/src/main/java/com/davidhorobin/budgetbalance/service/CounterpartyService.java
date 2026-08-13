package com.davidhorobin.budgetbalance.service;

import com.davidhorobin.budgetbalance.entity.Counterparty;
import com.davidhorobin.budgetbalance.repository.CounterpartyRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CounterpartyService {
    private final CounterpartyRepo counterpartyRepo;

    public Counterparty resolveOrCreateCounterparty(String name) {
        String normalisedName = name.trim().toLowerCase();
        return counterpartyRepo.findByName(normalisedName)
                .orElseGet(() -> {
                    Counterparty counterparty = new Counterparty();
                    counterparty.setName(normalisedName);
                    return counterpartyRepo.save(counterparty);
                });
    }
}
