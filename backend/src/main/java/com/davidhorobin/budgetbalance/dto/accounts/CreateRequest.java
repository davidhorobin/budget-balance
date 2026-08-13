package com.davidhorobin.budgetbalance.dto.accounts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateRequest(
        @NotBlank String name,
        @NotBlank String bank,
        @NotNull BigDecimal balance,
        @NotBlank String currency
) {
}
