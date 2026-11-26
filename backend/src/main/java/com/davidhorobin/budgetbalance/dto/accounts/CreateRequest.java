package com.davidhorobin.budgetbalance.dto.accounts;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Limits mirror the bank_accounts columns: VARCHAR(100), NUMERIC(12, 2), currency CHECK.
public record CreateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String bank,
        @NotNull @Digits(integer = 10, fraction = 2) BigDecimal balance, // may be negative (overdrawn)
        @NotBlank @Pattern(regexp = "GBP|USD|EUR") String currency
) {
}
