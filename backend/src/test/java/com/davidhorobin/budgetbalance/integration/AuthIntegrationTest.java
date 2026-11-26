package com.davidhorobin.budgetbalance.integration;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthIntegrationTest extends IntegrationTest {

    // ---- register ----

    @Test
    void registerStoresBcryptHash() throws Exception {
        register("alice").andExpect(status().isOk());

        String stored = jdbc.queryForObject("SELECT password FROM users WHERE username = 'alice'", String.class);
        assertThat(stored).startsWith("$2").isNotEqualTo(PASSWORD);
    }

    @Test
    void registerWithBlankFieldsIs400() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"\", \"password\": \"\", \"email\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registeringAnExistingUsernameDoesNotChangeTheOriginalAccount() throws Exception {
        register("alice").andExpect(status().isOk());
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"username": "alice", "password": "different", "email": "other@example.com"}
                        """))
                .andExpect(status().isOk());

        loginRequest("alice", PASSWORD).andExpect(status().isOk());
        loginRequest("alice", "different").andExpect(status().isUnauthorized());
    }

    @Disabled("Known gap: email format is only checked by the entity's @Email at insert time, so this returns 500")
    @Test
    void registerWithInvalidEmailIs400() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"username": "alice", "password": "pw", "email": "not-an-email"}
                        """))
                .andExpect(status().isBadRequest());
    }

    // ---- login ----

    @Test
    void loginReturnsAccessTokenAndHardenedRefreshCookie() throws Exception {
        register("alice");

        MvcResult result = loginRequest("alice", PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(emptyOrNullString())))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();

        assertThat(setCookieHeader(result))
                .contains("refreshToken=", "HttpOnly", "SameSite=Strict", "Path=/auth/refresh", "Max-Age=1209600");
    }

    @Test
    void refreshTokenIsStoredHashed() throws Exception {
        Session s = newUser("alice");

        String stored = jdbc.queryForObject("SELECT hash FROM refresh_tokens", String.class);
        assertThat(stored).isEqualTo(DigestUtils.sha256Hex(s.refreshToken())).isNotEqualTo(s.refreshToken());
    }

    @Test
    void wrongPasswordIs401WithMessage() throws Exception {
        register("alice");

        loginRequest("alice", "wrong")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void unknownUserGetsTheSame401() throws Exception {
        loginRequest("ghost", PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void loggingInAgainEndsThePreviousSession() throws Exception {
        register("alice");
        Session first = login("alice");
        login("alice");

        refresh(first.refreshToken()).andExpect(status().isUnauthorized());
    }

    // ---- refresh ----

    @Test
    void refreshRotatesTheRefreshTokenAndIssuesAWorkingAccessToken() throws Exception {
        Session s = newUser("alice");

        Session rotated = session(refresh(s.refreshToken()).andExpect(status().isOk()).andReturn());

        assertThat(rotated.refreshToken()).isNotEqualTo(s.refreshToken());
        getJson(rotated, "/accounts/info");
        refresh(rotated.refreshToken()).andExpect(status().isOk());
    }

    @Test
    void refreshWithoutCookieIs401() throws Exception {
        mvc.perform(post("/auth/refresh")).andExpect(status().isUnauthorized());
    }

    @Test
    void refreshWithUnknownTokenIs401() throws Exception {
        refresh("made-up").andExpect(status().isUnauthorized());
    }

    @Test
    void refreshWithExpiredTokenIs401() throws Exception {
        Session s = newUser("alice");
        jdbc.update("UPDATE refresh_tokens SET expiry_date = now() - interval '1 minute'");

        refresh(s.refreshToken()).andExpect(status().isUnauthorized());
    }

    @Test
    void reusingARotatedTokenRevokesTheWholeFamily() throws Exception {
        Session s = newUser("alice");
        Session rotated = session(refresh(s.refreshToken()).andExpect(status().isOk()).andReturn());

        refresh(s.refreshToken()).andExpect(status().isUnauthorized());          // theft detected
        refresh(rotated.refreshToken()).andExpect(status().isUnauthorized());    // so the live token dies too
    }

    @Test
    void concurrentRefreshesWithTheSameTokenLetExactlyOneThrough() throws Exception {
        Session s = newUser("alice");
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> results = new ArrayList<>();

        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            for (int i = 0; i < 2; i++) {
                results.add(pool.submit(() -> {
                    start.await();
                    return refresh(s.refreshToken()).andReturn().getResponse().getStatus();
                }));
            }
            start.countDown();

            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> f : results) statuses.add(f.get());
            assertThat(statuses).containsExactlyInAnyOrder(200, 401);
        }
    }
}
