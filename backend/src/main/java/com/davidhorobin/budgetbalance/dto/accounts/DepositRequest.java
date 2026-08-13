package com.davidhorobin.budgetbalance.dto.accounts;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DepositRequest(
        @NotBlank String accountName,
        @Positive @NotNull BigDecimal amount,
        @NotBlank String currency,
        @Nullable LocalDateTime time
) {
}
