package com.davidhorobin.budgetbalance.service;

import com.davidhorobin.budgetbalance.dto.accounts.CreateRequest;
import com.davidhorobin.budgetbalance.dto.accounts.CreateResponse;
import com.davidhorobin.budgetbalance.dto.accounts.InfoResponse;
import com.davidhorobin.budgetbalance.entity.BankAccount;
import com.davidhorobin.budgetbalance.entity.Counterparty;
import com.davidhorobin.budgetbalance.entity.User;
import com.davidhorobin.budgetbalance.repository.AccountsRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountsServiceTest {

    @Mock
    AccountsRepo accountsRepo;
    @Mock
    UserService userService;
    @Mock
    CounterpartyService counterpartyService;

    AccountsService service;
    User alice;

    @BeforeEach
    void setUp() {
        service = new AccountsService(accountsRepo, userService, counterpartyService);
        alice = new User();
        alice.setId(5);
        when(userService.getCurrentUser()).thenReturn(alice);
    }

    private static BankAccount account(String name, String balance) {
        BankAccount a = new BankAccount();
        a.setName(name);
        a.setBalance(new BigDecimal(balance));
        a.setCurrency("GBP");
        a.setCounterparty(new Counterparty(1, "hsbc"));
        return a;
    }

    @Test
    void createNormalisesNameAndSaves() {
        when(accountsRepo.findByNameAndUserId("current", 5L)).thenReturn(Optional.empty());
        when(counterpartyService.resolveOrCreateCounterparty("HSBC")).thenReturn(new Counterparty(1, "hsbc"));
        when(accountsRepo.save(any(BankAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateResponse response = service.createAccount(
                new CreateRequest("  Current ", "HSBC", new BigDecimal("150.00"), "GBP"));

        assertThat(response.name()).isEqualTo("current");
        assertThat(response.bank()).isEqualTo("hsbc");
        assertThat(response.balance()).isEqualByComparingTo("150.00");
        verify(accountsRepo).save(argThat(a -> a.getUser() == alice && a.getCurrency().equals("GBP")));
    }

    @Test
    void duplicateNameIs409AndSavesNothing() {
        when(accountsRepo.findByNameAndUserId("current", 5L)).thenReturn(Optional.of(new BankAccount()));

        assertThatThrownBy(() -> service.createAccount(
                new CreateRequest("Current", "HSBC", BigDecimal.ONE, "GBP")))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(e.getReason()).isEqualTo("You already have an account named 'current'.");
                });
        verify(accountsRepo, never()).save(any());
        verifyNoInteractions(counterpartyService);
    }

    @Test
    void getAllAccountsSumsBalancesIncludingNegatives() {
        when(accountsRepo.findAllByUserId(5L)).thenReturn(List.of(
                account("current", "100.50"), account("credit", "-40.25")));

        InfoResponse info = service.getAllAccounts();

        assertThat(info.sum()).isEqualByComparingTo("60.25");
        assertThat(info.accounts()).extracting("name").containsExactly("current", "credit");
    }

    @Test
    void getAllAccountsWithNoAccountsSumsToZero() {
        when(accountsRepo.findAllByUserId(5L)).thenReturn(List.of());

        InfoResponse info = service.getAllAccounts();

        assertThat(info.sum()).isEqualByComparingTo("0");
        assertThat(info.accounts()).isEmpty();
    }

    @Test
    void resolveBankAccountNormalisesNameAndScopesToCurrentUser() {
        BankAccount current = account("current", "1");
        when(accountsRepo.findByNameAndUserId("current", 5L)).thenReturn(Optional.of(current));

        assertThat(service.resolveBankAccount("  CURRENT ")).isSameAs(current);
    }

    @Test
    void resolveBankAccountReturnsNullWhenMissing() {
        when(accountsRepo.findByNameAndUserId("nope", 5L)).thenReturn(Optional.empty());

        assertThat(service.resolveBankAccount("nope")).isNull();
    }
}
