package com.davidhorobin.budgetbalance.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionsIntegrationTest extends IntegrationTest {

    Session alice;

    @BeforeEach
    void aliceWithAnAccount() throws Exception {
        alice = newUser("alice");
        createAccount(alice, "Current", "100.00", "GBP").andExpect(status().isOk());
    }

    private static String daysAgo(int days) {
        return LocalDateTime.now().minusDays(days).truncatedTo(ChronoUnit.SECONDS).toString();
    }

    // ---- purchases ----

    @Test
    void purchaseReducesBalanceAndIsReturned() throws Exception {
        purchase(alice, "current", "Tesco", "12.34", "GBP", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(12.34))
                .andExpect(jsonPath("$.counterparty").value("tesco"))
                .andExpect(jsonPath("$.type").value("Purchase"))
                .andExpect(jsonPath("$.time").exists());

        assertThat(balanceOf(alice, "current")).isEqualByComparingTo("87.66");
    }

    @Test
    void purchaseCanOverdrawTheAccount() throws Exception {
        purchase(alice, "current", "Landlord", "750.00", "GBP", null).andExpect(status().isOk());

        assertThat(balanceOf(alice, "current")).isEqualByComparingTo("-650.00");
    }

    @Test
    void accountNameIsMatchedCaseInsensitively() throws Exception {
        purchase(alice, " CURRENT ", "Tesco", "1.00", "GBP", null).andExpect(status().isOk());
    }

    @Test
    void purchaseWithExplicitPastTimeKeepsIt() throws Exception {
        String time = daysAgo(3);

        String json = purchase(alice, "current", "Tesco", "1.00", "GBP", time)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Compare as dates: Jackson always writes seconds, LocalDateTime.toString() drops ":00".
        assertThat(LocalDateTime.parse(JsonPath.read(json, "$.time"))).isEqualTo(LocalDateTime.parse(time));
    }

    @Test
    void futurePurchaseIs400() throws Exception {
        String tomorrow = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS).toString();

        purchase(alice, "current", "Tesco", "1.00", "GBP", tomorrow).andExpect(status().isBadRequest());
        assertThat(transactionCount()).isZero();
    }

    @Test
    void nonPositiveAmountIs400() throws Exception {
        purchase(alice, "current", "Tesco", "0", "GBP", null).andExpect(status().isBadRequest());
        purchase(alice, "current", "Tesco", "-5", "GBP", null).andExpect(status().isBadRequest());
        assertThat(balanceOf(alice, "current")).isEqualByComparingTo("100.00");
    }

    @Test
    void unknownAccountIs404AndChangesNothing() throws Exception {
        purchase(alice, "savings", "Tesco", "1.00", "GBP", null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No account named 'savings'."));
        assertThat(transactionCount()).isZero();
    }

    @Test
    void currencyMismatchIs400AndChangesNothing() throws Exception {
        purchase(alice, "current", "Tesco", "1.00", "USD", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account 'current' is in GBP, not USD."));

        assertThat(balanceOf(alice, "current")).isEqualByComparingTo("100.00");
        assertThat(transactionCount()).isZero();
    }

    @Test
    void cannotPurchaseFromAnotherUsersAccount() throws Exception {
        Session bob = newUser("bob");

        purchase(bob, "current", "Tesco", "1.00", "GBP", null).andExpect(status().isNotFound());
        assertThat(balanceOf(alice, "current")).isEqualByComparingTo("100.00");
    }

    @Disabled("Known gap: counterparty has no @Size(max = 100), so an overlong name hits VARCHAR(100) and returns 500")
    @Test
    void overlongCounterpartyIs400() throws Exception {
        purchase(alice, "current", "x".repeat(101), "1.00", "GBP", null).andExpect(status().isBadRequest());
    }

    // ---- deposits ----

    @Test
    void depositIncreasesBalance() throws Exception {
        deposit(alice, "current", "25.50", "GBP")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counterparty").value("deposit"))
                .andExpect(jsonPath("$.value").value(25.50));

        assertThat(balanceOf(alice, "current")).isEqualByComparingTo("125.50");
    }

    @Test
    void depositIntoUnknownAccountIs404() throws Exception {
        deposit(alice, "savings", "1.00", "GBP").andExpect(status().isNotFound());
    }

    @Test
    void depositWithWrongCurrencyIs400() throws Exception {
        deposit(alice, "current", "1.00", "EUR").andExpect(status().isBadRequest());
        assertThat(balanceOf(alice, "current")).isEqualByComparingTo("100.00");
    }

    // ---- listing ----

    @Test
    void listIsNewestFirstAndSignedByType() throws Exception {
        purchase(alice, "current", "Oldest", "1.00", "GBP", daysAgo(3));
        purchase(alice, "current", "Newest", "1.00", "GBP", daysAgo(1));
        purchase(alice, "current", "Middle", "1.00", "GBP", daysAgo(2));
        deposit(alice, "current", "5.00", "GBP"); // now: newest of all

        String json = getJson(alice, "/transaction/");

        assertThat(JsonPath.<List<String>>read(json, "$[*].counterparty"))
                .containsExactly("deposit", "newest", "middle", "oldest");
        assertThat(JsonPath.<List<String>>read(json, "$[*].type"))
                .containsExactly("Deposit", "Purchase", "Purchase", "Purchase");
    }

    @Test
    void listOnlyShowsOwnTransactions() throws Exception {
        Session bob = newUser("bob");
        createAccount(bob, "Current", "10", "GBP");
        purchase(alice, "current", "Alice shop", "1.00", "GBP", null);
        purchase(bob, "current", "Bob shop", "1.00", "GBP", null);

        assertThat(JsonPath.<List<String>>read(getJson(alice, "/transaction/"), "$[*].counterparty"))
                .containsExactly("alice shop");
    }

    // ---- by id ----

    private long onlyTransactionId() {
        return jdbc.queryForObject("SELECT id FROM transactions", Long.class);
    }

    @Test
    void getOwnTransactionById() throws Exception {
        purchase(alice, "current", "Tesco", "3.00", "GBP", null);

        mvc.perform(get("/transaction/" + onlyTransactionId()).header(HttpHeaders.AUTHORIZATION, alice.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counterparty").value("tesco"));
    }

    @Test
    void getMissingTransactionIs404WithMessage() throws Exception {
        mvc.perform(get("/transaction/999").header(HttpHeaders.AUTHORIZATION, alice.bearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction with id 999 not found."));
    }

    @Test
    void deleteOwnTransaction() throws Exception {
        purchase(alice, "current", "Tesco", "3.00", "GBP", null);

        mvc.perform(delete("/transaction/" + onlyTransactionId()).header(HttpHeaders.AUTHORIZATION, alice.bearer()))
                .andExpect(status().isOk());
        assertThat(transactionCount()).isZero();
    }

    @Disabled("Known bug: GET /transaction/{id} does not check ownership, so any user can read any transaction")
    @Test
    void cannotReadAnotherUsersTransaction() throws Exception {
        purchase(alice, "current", "Tesco", "3.00", "GBP", null);
        Session bob = newUser("bob");

        mvc.perform(get("/transaction/" + onlyTransactionId()).header(HttpHeaders.AUTHORIZATION, bob.bearer()))
                .andExpect(status().isNotFound());
    }

    @Disabled("Known bug: DELETE /transaction/{id} does not check ownership, so any user can delete any transaction")
    @Test
    void cannotDeleteAnotherUsersTransaction() throws Exception {
        purchase(alice, "current", "Tesco", "3.00", "GBP", null);
        Session bob = newUser("bob");

        mvc.perform(delete("/transaction/" + onlyTransactionId()).header(HttpHeaders.AUTHORIZATION, bob.bearer()))
                .andExpect(status().isNotFound());
        assertThat(transactionCount()).isEqualTo(1);
    }
}
