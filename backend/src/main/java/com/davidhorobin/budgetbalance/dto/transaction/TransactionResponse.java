package com.davidhorobin.budgetbalance.dto.transaction;

import com.davidhorobin.budgetbalance.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        BigDecimal value,
        String counterparty,
        LocalDateTime time,
        TransactionType type
) {
}
