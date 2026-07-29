package com.davidhorobin.budgetbalance.dto.transaction;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionRequest(
        @NotNull @Positive BigDecimal amount,
        @NotBlank String counterparty,
        @Nullable LocalDateTime time
) {
}