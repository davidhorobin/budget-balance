package com.davidhorobin.budgetbalance.integration;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full application against the budget_balance_test Postgres database, requests sent through the
 * real security filter chain. Every table is emptied before each test.
 * JSON is written by hand on purpose: it checks the real snake_case wire format.
 */
@SpringBootTest
@ActiveProfiles("test")
abstract class IntegrationTest {

    static final String PASSWORD = "Passw0rd!";

    @Autowired
    WebApplicationContext context;
    @Autowired
    JdbcTemplate jdbc;

    MockMvc mvc;

    record Session(String accessToken, String refreshToken) {
        String bearer() {
            return "Bearer " + accessToken;
        }
    }

    @BeforeEach
    void resetDatabase() {
        // Guard: the TRUNCATE below must never run against a real database.
        String db = jdbc.queryForObject("SELECT current_database()", String.class);
        assertThat(db).as("integration tests only run against a *_test database").endsWith("_test");
        jdbc.execute("TRUNCATE transactions, refresh_tokens, bank_accounts, counterparties, users RESTART IDENTITY CASCADE");

        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    // ---- auth helpers ----

    ResultActions register(String username) throws Exception {
        return mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"username": "%s", "password": "%s", "email": "%s@example.com"}
                """.formatted(username, PASSWORD, username)));
    }

    ResultActions loginRequest(String username, String password) throws Exception {
        return mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                {"username": "%s", "password": "%s"}
                """.formatted(username, password)));
    }

    Session login(String username) throws Exception {
        MvcResult result = loginRequest(username, PASSWORD).andExpect(status().isOk()).andReturn();
        return session(result);
    }

    /** Registers and logs in a fresh user. */
    Session newUser(String username) throws Exception {
        register(username).andExpect(status().isOk());
        return login(username);
    }

    ResultActions refresh(String refreshToken) throws Exception {
        return mvc.perform(post("/auth/refresh").cookie(new Cookie("refreshToken", refreshToken)));
    }

    static Session session(MvcResult result) throws Exception {
        String accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
        Cookie cookie = result.getResponse().getCookie("refreshToken");
        assertThat(cookie).as("refreshToken cookie").isNotNull();
        return new Session(accessToken, cookie.getValue());
    }

    static String setCookieHeader(MvcResult result) {
        return result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
    }

    // ---- account / transaction helpers ----

    ResultActions createAccount(Session s, String name, String balance, String currency) throws Exception {
        return mvc.perform(post("/accounts/new").header(HttpHeaders.AUTHORIZATION, s.bearer())
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "%s", "bank": "HSBC", "balance": %s, "currency": "%s"}
                        """.formatted(name, balance, currency)));
    }

    ResultActions purchase(Session s, String account, String counterparty, String amount, String currency, String time)
            throws Exception {
        return mvc.perform(post("/transaction/").header(HttpHeaders.AUTHORIZATION, s.bearer())
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"bank_account": "%s", "counterparty": "%s", "amount": %s, "currency": "%s", "time": %s}
                        """.formatted(account, counterparty, amount, currency, time == null ? "null" : "\"" + time + "\"")));
    }

    ResultActions deposit(Session s, String account, String amount, String currency) throws Exception {
        return mvc.perform(patch("/accounts/deposit").header(HttpHeaders.AUTHORIZATION, s.bearer())
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"account_name": "%s", "amount": %s, "currency": "%s"}
                        """.formatted(account, amount, currency)));
    }

    String getJson(Session s, String url) throws Exception {
        return mvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, s.bearer()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    BigDecimal balanceOf(Session s, String account) throws Exception {
        List<Object> balances = JsonPath.read(getJson(s, "/accounts/info"),
                "$.accounts[?(@.name == '" + account + "')].balance");
        assertThat(balances).as("account " + account).hasSize(1);
        return new BigDecimal(balances.getFirst().toString());
    }

    long transactionCount() {
        return jdbc.queryForObject("SELECT count(*) FROM transactions", Long.class);
    }
}
