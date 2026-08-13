package com.davidhorobin.budgetbalance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(
        name = "bank_accounts",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_name_user",
                columnNames = {"name", "user_id"}
        )
)
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "counterparty_id", nullable = false)
    private Counterparty counterparty;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    @NotNull
    @Pattern(regexp = "GBP|USD|EUR")
    private String currency;
}
