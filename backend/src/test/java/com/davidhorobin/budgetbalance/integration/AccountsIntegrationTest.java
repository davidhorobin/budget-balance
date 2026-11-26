package com.davidhorobin.budgetbalance.integration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountsIntegrationTest extends IntegrationTest {

    @Test
    void createAccountNormalisesNameAndBank() throws Exception {
        Session s = newUser("alice");

        createAccount(s, "  Current ", "150.00", "GBP")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("current"))
                .andExpect(jsonPath("$.bank").value("hsbc"))
                .andExpect(jsonPath("$.balance").value(150.00));
    }

    @Test
    void negativeOpeningBalanceIsAllowed() throws Exception {
        Session s = newUser("alice");

        createAccount(s, "Credit card", "-250.00", "GBP").andExpect(status().isOk());

        assertThat(balanceOf(s, "credit card")).isEqualByComparingTo("-250.00");
    }

    @Test
    void duplicateNameIs409RegardlessOfCase() throws Exception {
        Session s = newUser("alice");
        createAccount(s, "Current", "1", "GBP").andExpect(status().isOk());

        createAccount(s, "CURRENT ", "1", "GBP")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("You already have an account named 'current'."));
    }

    @Test
    void differentUsersMayUseTheSameAccountName() throws Exception {
        createAccount(newUser("alice"), "Current", "1", "GBP").andExpect(status().isOk());
        createAccount(newUser("bob"), "Current", "1", "GBP").andExpect(status().isOk());
    }

    @Test
    void unsupportedCurrencyIs400() throws Exception {
        createAccount(newUser("alice"), "Current", "1", "JPY").andExpect(status().isBadRequest());
    }

    @Test
    void moreThanTwoDecimalPlacesIs400() throws Exception {
        createAccount(newUser("alice"), "Current", "1.005", "GBP").andExpect(status().isBadRequest());
    }

    @Test
    void infoListsOnlyOwnAccountsAndSumsThem() throws Exception {
        Session alice = newUser("alice");
        Session bob = newUser("bob");
        createAccount(alice, "Current", "100.50", "GBP");
        createAccount(alice, "Credit", "-40.25", "GBP");
        createAccount(bob, "Savings", "999", "GBP");

        String json = getJson(alice, "/accounts/info");

        assertThat(com.jayway.jsonpath.JsonPath.<Integer>read(json, "$.accounts.length()")).isEqualTo(2);
        assertThat(new java.math.BigDecimal(com.jayway.jsonpath.JsonPath.read(json, "$.sum").toString()))
                .isEqualByComparingTo("60.25");
    }

    @Test
    void newUserHasNoAccounts() throws Exception {
        String json = getJson(newUser("alice"), "/accounts/info");

        assertThat(com.jayway.jsonpath.JsonPath.<Integer>read(json, "$.accounts.length()")).isZero();
    }
}
