package com.davidhorobin.budgetbalance.dto.accounts;

import java.math.BigDecimal;

public record AccountInfo(
        String name,
        String bank,
        BigDecimal balance,
        String currency
) {
}
