package com.davidhorobin.budgetbalance.dto.accounts;

import java.math.BigDecimal;

public record CreateResponse(
        String name,
        BigDecimal balance,
        String bank
) {
}
