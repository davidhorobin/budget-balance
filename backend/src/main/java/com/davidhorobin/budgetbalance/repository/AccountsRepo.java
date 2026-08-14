package com.davidhorobin.budgetbalance.repository;

import com.davidhorobin.budgetbalance.entity.BankAccount;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountsRepo extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByNameAndUserId(String name, long userId);

    @Modifying
    @Transactional
    @Query("UPDATE BankAccount b SET b.balance = b.balance + :amount WHERE b.id = :id AND b.balance + :amount >= 0")
    int adjustBalance(@Param("id") long id, @Param("amount") BigDecimal amount);

    List<BankAccount> findAllByUserId(long id);
}
