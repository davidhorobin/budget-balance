package com.davidhorobin.budgetbalance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "counterparty_id", nullable = false)
    private Counterparty counterparty;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @NotNull
    private String currency;

    @Column(nullable = false)
    @PastOrPresent
    private LocalDateTime time;

}