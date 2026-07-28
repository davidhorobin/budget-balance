package com.davidhorobin.budgetbalance.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse (
        Integer id,
        BigDecimal value,
        String vendor,
        LocalDateTime time
) {}
