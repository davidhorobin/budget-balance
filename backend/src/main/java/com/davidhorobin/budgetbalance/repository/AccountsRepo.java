package com.davidhorobin.budgetbalance.repository;

import com.davidhorobin.budgetbalance.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountsRepo extends JpaRepository<BankAccount, Long> {
}
