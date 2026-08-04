package com.davidhorobin.budgetbalance.dto.auth;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
