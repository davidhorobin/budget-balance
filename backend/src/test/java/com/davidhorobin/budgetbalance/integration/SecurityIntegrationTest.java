package com.davidhorobin.budgetbalance.integration;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Base64;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityIntegrationTest extends IntegrationTest {

    @Value("${jwt.secret}")
    String secret;

    private String tokenFor(String username, long expiresInMillis) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiresInMillis))
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret)))
                .compact();
    }

    private void expect401With(String authorization) throws Exception {
        mvc.perform(get("/accounts/info").header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validTokenIsAccepted() throws Exception {
        getJson(newUser("alice"), "/accounts/info");
    }

    @Test
    void missingTokenIs401() throws Exception {
        mvc.perform(get("/accounts/info")).andExpect(status().isUnauthorized());
    }

    @Test
    void garbageTokenIs401() throws Exception {
        expect401With("Bearer garbage");
    }

    @Test
    void undefinedTokenFromFrontendIs401() throws Exception {
        expect401With("Bearer undefined");
    }

    @Test
    void emptyBearerIs401() throws Exception {
        expect401With("Bearer ");
    }

    @Test
    void expiredTokenIs401() throws Exception {
        register("alice");
        expect401With("Bearer " + tokenFor("alice", -1_000));
    }

    @Test
    void tokenSignedWithAnotherKeyIs401() throws Exception {
        register("alice");
        String forged = Jwts.builder().subject("alice")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Jwts.SIG.HS256.key().build())
                .compact();
        expect401With("Bearer " + forged);
    }

    @Test
    void validTokenForDeletedUserIs401() throws Exception {
        expect401With("Bearer " + tokenFor("ghost", 60_000));
    }

    @Test
    void nonBearerSchemeIs401() throws Exception {
        expect401With("Basic YWxpY2U6cHc=");
    }

    @Test
    void protectedEndpointsAllRequireAuth() throws Exception {
        mvc.perform(get("/transaction/")).andExpect(status().isUnauthorized());
        mvc.perform(get("/transaction/1")).andExpect(status().isUnauthorized());
    }

    // ---- CORS ----

    @Test
    void corsPreflightAllowsPatchFromFrontend() throws Exception {
        mvc.perform(options("/accounts/deposit")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.PATCH.name()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    void corsPreflightRejectsOtherOrigins() throws Exception {
        mvc.perform(options("/accounts/info")
                        .header(HttpHeaders.ORIGIN, "https://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name()))
                .andExpect(status().isForbidden());
    }
}
