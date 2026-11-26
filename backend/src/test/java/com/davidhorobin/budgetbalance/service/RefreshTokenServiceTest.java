package com.davidhorobin.budgetbalance.service;

import com.davidhorobin.budgetbalance.dto.auth.RefreshResponse;
import com.davidhorobin.budgetbalance.entity.RefreshToken;
import com.davidhorobin.budgetbalance.entity.User;
import com.davidhorobin.budgetbalance.repository.RefreshTokenRepo;
import com.davidhorobin.budgetbalance.security.jwt.JwtService;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    RefreshTokenRepo repo;
    @Mock
    JwtService jwtService;

    RefreshTokenService service;
    User user;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(repo, jwtService);
        user = new User();
        user.setId(7);
        user.setUsername("alice");
    }

    private RefreshToken stored(String raw, boolean revoked, Instant expiry) {
        RefreshToken t = new RefreshToken();
        t.setId(42);
        t.setToken(DigestUtils.sha256Hex(raw));
        t.setUser(user);
        t.setRevoked(revoked);
        t.setExpiryDate(expiry);
        return t;
    }

    private void found(RefreshToken t) {
        when(repo.findByToken(t.getToken())).thenReturn(Optional.of(t));
    }

    @Test
    void createStoresOnlyTheHashWithFourteenDayExpiry() {
        Instant before = Instant.now();
        String raw = service.create(user);

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repo).save(saved.capture());
        assertThat(saved.getValue().getToken()).isEqualTo(DigestUtils.sha256Hex(raw)).isNotEqualTo(raw);
        assertThat(saved.getValue().getUser()).isSameAs(user);
        assertThat(saved.getValue().getExpiryDate())
                .isBetween(before.plus(Duration.ofDays(14)), Instant.now().plus(Duration.ofDays(14)));
    }

    @Test
    void createReturnsADifferentTokenEachTime() {
        assertThat(service.create(user)).isNotEqualTo(service.create(user));
    }

    @Test
    void unknownTokenFails() {
        when(repo.findByToken(any())).thenReturn(Optional.empty());

        assertThat(service.verify("nope").success()).isFalse();
        verify(repo, never()).revokeAllByUserId(anyLong());
    }

    @Test
    void reusedRevokedTokenFailsAndRevokesEveryTokenOfTheUser() {
        found(stored("old", true, Instant.now().plusSeconds(60)));

        assertThat(service.verify("old").success()).isFalse();
        verify(repo).revokeAllByUserId(7L);
        verify(repo, never()).save(any());
    }

    @Test
    void expiredTokenFails() {
        found(stored("raw", false, Instant.now().minusSeconds(1)));

        assertThat(service.verify("raw").success()).isFalse();
        verify(repo, never()).revokeIfActive(anyLong());
        verify(repo, never()).save(any());
    }

    @Test
    void validTokenIsRotated() {
        found(stored("raw", false, Instant.now().plusSeconds(60)));
        when(repo.revokeIfActive(42L)).thenReturn(1);
        when(jwtService.generateAccessToken(user)).thenReturn("new-access");

        RefreshResponse response = service.verify("raw");

        assertThat(response.success()).isTrue();
        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isNotBlank().isNotEqualTo("raw");
        verify(repo).revokeIfActive(42L);
        verify(repo).save(any(RefreshToken.class));
    }

    @Test
    void losingAConcurrentRotationFailsWithoutRevokingEverything() {
        found(stored("raw", false, Instant.now().plusSeconds(60)));
        when(repo.revokeIfActive(42L)).thenReturn(0);

        assertThat(service.verify("raw").success()).isFalse();
        verify(repo, never()).revokeAllByUserId(anyLong());
        verify(repo, never()).save(any());
        verifyNoInteractions(jwtService);
    }

    @Test
    void invalidateOtherTokensKeepsTheGivenOne() {
        RefreshToken current = stored("raw", false, Instant.now().plusSeconds(60));
        found(current);

        service.invalidateOtherTokens("raw");

        verify(repo).revokeAllExceptCurrent(7L, current.getToken());
    }

    @Test
    void invalidateOtherTokensIgnoresUnknownToken() {
        when(repo.findByToken(any())).thenReturn(Optional.empty());

        assertThat(service.invalidateOtherTokens("nope")).isFalse();
        verify(repo, never()).revokeAllExceptCurrent(anyLong(), any());
    }
}
