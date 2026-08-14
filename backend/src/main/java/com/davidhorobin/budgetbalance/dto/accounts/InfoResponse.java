package com.davidhorobin.budgetbalance.dto.accounts;

import com.davidhorobin.budgetbalance.entity.BankAccount;

import java.math.BigDecimal;
import java.util.List;

public record InfoResponse(
        BigDecimal sum,
        List<BankAccount> accounts
) {
}
