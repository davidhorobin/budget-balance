package com.davidhorobin.budgetbalance.dto.accounts;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DepositResponse(
        String counterparty,
        BigDecimal value,
        LocalDateTime time
) {
}
