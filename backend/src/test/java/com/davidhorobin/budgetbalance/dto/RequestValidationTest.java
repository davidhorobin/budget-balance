package com.davidhorobin.budgetbalance.dto;

import com.davidhorobin.budgetbalance.dto.accounts.CreateRequest;
import com.davidhorobin.budgetbalance.dto.accounts.DepositRequest;
import com.davidhorobin.budgetbalance.dto.auth.LoginRequest;
import com.davidhorobin.budgetbalance.dto.auth.RegisterRequest;
import com.davidhorobin.budgetbalance.dto.transaction.TransactionRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/** Bean Validation rules on request bodies: what the API turns into a 400. */
class RequestValidationTest {

    private static jakarta.validation.ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    private static Set<String> invalidFields(Object request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    private static CreateRequest account(String name, String balance, String currency) {
        return new CreateRequest(name, "HSBC", new BigDecimal(balance), currency);
    }

    // ---- CreateRequest ----

    @Test
    void validAccountPasses() {
        assertThat(invalidFields(account("Current", "150.00", "GBP"))).isEmpty();
    }

    @Test
    void negativeOpeningBalanceIsAllowed() {
        assertThat(invalidFields(account("Credit card", "-250.00", "GBP"))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"JPY", "gbp", "GB", ""})
    void unsupportedCurrencyIsRejected(String currency) {
        assertThat(invalidFields(account("Current", "1", currency))).contains("currency");
    }

    @Test
    void moreThanTwoDecimalPlacesIsRejected() {
        assertThat(invalidFields(account("Current", "1.005", "GBP"))).contains("balance");
    }

    @Test
    void balanceTooLargeForTheColumnIsRejected() {
        assertThat(invalidFields(account("Current", "12345678901.00", "GBP"))).contains("balance");
    }

    @Test
    void accountNameOver100CharactersIsRejected() {
        assertThat(invalidFields(account("x".repeat(101), "1", "GBP"))).contains("name");
    }

    @Test
    void blankAccountFieldsAreRejected() {
        assertThat(invalidFields(new CreateRequest(" ", " ", null, null)))
                .containsExactlyInAnyOrder("name", "bank", "balance", "currency");
    }

    // ---- TransactionRequest ----

    private static TransactionRequest purchase(String amount, LocalDateTime time) {
        return new TransactionRequest("current", "Tesco", new BigDecimal(amount), "GBP", time);
    }

    @Test
    void validPurchasePasses() {
        assertThat(invalidFields(purchase("4.50", null))).isEmpty();
        assertThat(invalidFields(purchase("4.50", LocalDateTime.now().minusDays(1)))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1.00"})
    void nonPositivePurchaseAmountIsRejected(String amount) {
        assertThat(invalidFields(purchase(amount, null))).contains("amount");
    }

    @Test
    void futurePurchaseIsRejected() {
        assertThat(invalidFields(purchase("1", LocalDateTime.now().plusDays(1)))).contains("time");
    }

    @Test
    void blankPurchaseFieldsAreRejected() {
        assertThat(invalidFields(new TransactionRequest("", "", null, null, null)))
                .containsExactlyInAnyOrder("bankAccount", "counterparty", "amount", "currency");
    }

    // ---- DepositRequest ----

    @Test
    void validDepositPasses() {
        assertThat(invalidFields(new DepositRequest("current", BigDecimal.TEN, "GBP", null))).isEmpty();
    }

    @Test
    void futureDepositIsRejected() {
        assertThat(invalidFields(new DepositRequest("current", BigDecimal.TEN, "GBP", LocalDateTime.now().plusHours(1))))
                .contains("time");
    }

    @Test
    void nonPositiveDepositIsRejected() {
        assertThat(invalidFields(new DepositRequest("current", BigDecimal.ZERO, "GBP", null))).contains("amount");
    }

    // ---- Auth ----

    @Test
    void blankLoginFieldsAreRejected() {
        assertThat(invalidFields(new LoginRequest("", ""))).containsExactlyInAnyOrder("username", "password");
    }

    @Test
    void blankRegisterFieldsAreRejected() {
        assertThat(invalidFields(new RegisterRequest("", "", "")))
                .containsExactlyInAnyOrder("username", "password", "email");
    }
}
