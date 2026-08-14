package com.davidhorobin.budgetbalance.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        BigDecimal value,
        String counterparty,
        LocalDateTime time
) {
}
