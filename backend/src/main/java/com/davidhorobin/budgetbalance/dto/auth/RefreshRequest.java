package com.davidhorobin.budgetbalance.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank String token
) {
}
