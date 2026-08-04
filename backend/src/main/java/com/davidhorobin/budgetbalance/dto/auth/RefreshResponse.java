package com.davidhorobin.budgetbalance.dto.auth;

public record RefreshResponse(
        boolean success,
        String accessToken,
        String refreshToken
) {
    public RefreshResponse(boolean success) {
        this(success, null, null);
    }
}
