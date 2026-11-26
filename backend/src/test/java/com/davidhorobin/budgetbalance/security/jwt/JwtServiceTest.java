package com.davidhorobin.budgetbalance.security.jwt;

import com.davidhorobin.budgetbalance.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "VZDL69c3L6czkKdY6Rxu92BSwMnrksqEewHEei6Td4k=";
    private static final String OTHER_SECRET = "3q2+7w8Jm1t0Yb6k4XvPZr5cH9sLQe2nUa7dWfGhJkA=";

    private final JwtService jwtService = new JwtService(SECRET);

    private static User user(String username) {
        User u = new User();
        u.setUsername(username);
        return u;
    }

    private static SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    @Test
    void rejectsKeyShorterThan256Bits() {
        String shortKey = Base64.getEncoder().encodeToString(new byte[16]);
        assertThatThrownBy(() -> new JwtService(shortKey)).isInstanceOf(WeakKeyException.class);
    }

    @Test
    void generatedTokenCarriesUsernameAndValidates() {
        User alice = user("alice");
        String token = jwtService.generateAccessToken(alice);

        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
        assertThat(jwtService.validateToken(token, alice)).isTrue();
    }

    @Test
    void tokenIsNotValidForADifferentUser() {
        String token = jwtService.generateAccessToken(user("alice"));
        assertThat(jwtService.validateToken(token, user("bob"))).isFalse();
    }

    @Test
    void accessTokenExpiresAfterFifteenMinutes() {
        long before = System.currentTimeMillis();
        String token = jwtService.generateAccessToken(user("alice"));

        Date expiry = Jwts.parser().verifyWith(key(SECRET)).build()
                .parseSignedClaims(token).getPayload().getExpiration();

        long fifteenMinutes = 15 * 60 * 1000;
        // JWT dates have second precision, so allow a second either side.
        assertThat(expiry.getTime()).isBetween(before + fifteenMinutes - 1000, System.currentTimeMillis() + fifteenMinutes + 1000);
    }

    @Test
    void tokenSurvivesARestartWithTheSameSecret() {
        String token = jwtService.generateAccessToken(user("alice"));
        JwtService afterRestart = new JwtService(SECRET);

        assertThat(afterRestart.extractUsername(token)).isEqualTo("alice");
    }

    @Test
    void expiredTokenIsRejected() {
        String expired = Jwts.builder()
                .subject("alice")
                .issuedAt(new Date(System.currentTimeMillis() - 60_000))
                .expiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(key(SECRET))
                .compact();

        assertThatThrownBy(() -> jwtService.extractUsername(expired)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String forged = new JwtService(OTHER_SECRET).generateAccessToken(user("alice"));

        assertThatThrownBy(() -> jwtService.extractUsername(forged)).isInstanceOf(SignatureException.class);
    }

    @Test
    void malformedTokenIsRejected() {
        assertThatThrownBy(() -> jwtService.extractUsername("not-a-jwt")).isInstanceOf(MalformedJwtException.class);
    }
}
