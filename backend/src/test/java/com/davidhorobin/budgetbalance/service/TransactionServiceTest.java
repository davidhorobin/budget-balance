package com.davidhorobin.budgetbalance.service;

import com.davidhorobin.budgetbalance.dto.accounts.DepositRequest;
import com.davidhorobin.budgetbalance.dto.accounts.DepositResponse;
import com.davidhorobin.budgetbalance.dto.transaction.TransactionRequest;
import com.davidhorobin.budgetbalance.dto.transaction.TransactionResponse;
import com.davidhorobin.budgetbalance.entity.BankAccount;
import com.davidhorobin.budgetbalance.entity.Counterparty;
import com.davidhorobin.budgetbalance.entity.Transaction;
import com.davidhorobin.budgetbalance.entity.User;
import com.davidhorobin.budgetbalance.enums.TransactionType;
import com.davidhorobin.budgetbalance.repository.AccountsRepo;
import com.davidhorobin.budgetbalance.repository.TransactionRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    TransactionRepo transactionRepo;
    @Mock
    CounterpartyService counterpartyService;
    @Mock
    AccountsRepo accountsRepo;
    @Mock
    AccountsService accountsService;
    @Mock
    UserService userService;

    TransactionService service;
    BankAccount account;
    Counterparty tesco;

    @BeforeEach
    void setUp() {
        service = new TransactionService(transactionRepo, counterpartyService, accountsRepo, accountsService, userService);
        account = new BankAccount();
        account.setId(3);
        account.setName("current");
        account.setCurrency("GBP");
        account.setBalance(new BigDecimal("10.00"));
        tesco = new Counterparty(1, "tesco");
    }

    private void accountExists() {
        when(accountsService.resolveBankAccount("current")).thenReturn(account);
    }

    private void saveEchoesEntity() {
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static TransactionRequest purchase(String amount, String currency, LocalDateTime time) {
        return new TransactionRequest("current", "Tesco", new BigDecimal(amount), currency, time);
    }

    @Test
    void purchaseSubtractsFromBalanceAndRecordsTransaction() {
        accountExists();
        saveEchoesEntity();
        when(counterpartyService.resolveOrCreateCounterparty("Tesco")).thenReturn(tesco);

        TransactionResponse response = service.purchase(purchase("4.50", "GBP", null));

        verify(accountsRepo).adjustBalance(3L, new BigDecimal("-4.50"));
        assertThat(response.value()).isEqualByComparingTo("4.50");
        assertThat(response.counterparty()).isEqualTo("tesco");
        assertThat(response.type()).isEqualTo(TransactionType.Purchase);
    }

    @Test
    void purchaseLargerThanBalanceIsAllowed() {
        accountExists();
        saveEchoesEntity();
        when(counterpartyService.resolveOrCreateCounterparty("Tesco")).thenReturn(tesco);

        TransactionResponse response = service.purchase(purchase("500.00", "GBP", null));

        verify(accountsRepo).adjustBalance(3L, new BigDecimal("-500.00"));
        assertThat(response.type()).isEqualTo(TransactionType.Purchase);
    }

    @Test
    void depositAddsToBalance() {
        accountExists();
        saveEchoesEntity();
        Counterparty deposit = new Counterparty(2, "deposit");
        when(counterpartyService.resolveOrCreateCounterparty("Deposit")).thenReturn(deposit);

        DepositResponse response = service.deposit(new DepositRequest("current", new BigDecimal("25.00"), "GBP", null));

        verify(accountsRepo).adjustBalance(3L, new BigDecimal("25.00"));
        assertThat(response.value()).isEqualByComparingTo("25.00");
        assertThat(response.counterparty()).isEqualTo("deposit");

        ArgumentCaptor<Transaction> saved = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepo).save(saved.capture());
        assertThat(saved.getValue().getType()).isEqualTo(TransactionType.Deposit);
    }

    @Test
    void timeDefaultsToNow() {
        accountExists();
        saveEchoesEntity();
        when(counterpartyService.resolveOrCreateCounterparty("Tesco")).thenReturn(tesco);
        LocalDateTime before = LocalDateTime.now();

        TransactionResponse response = service.purchase(purchase("1.00", "GBP", null));

        assertThat(response.time()).isBetween(before, LocalDateTime.now());
    }

    @Test
    void suppliedTimeIsKept() {
        accountExists();
        saveEchoesEntity();
        when(counterpartyService.resolveOrCreateCounterparty("Tesco")).thenReturn(tesco);
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1).withNano(0);

        TransactionResponse response = service.purchase(purchase("1.00", "GBP", lastWeek));

        assertThat(response.time()).isEqualTo(lastWeek);
    }

    @Test
    void unknownAccountIs404AndChangesNothing() {
        when(accountsService.resolveBankAccount("current")).thenReturn(null);

        assertThatThrownBy(() -> service.purchase(purchase("1.00", "GBP", null)))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(e.getReason()).isEqualTo("No account named 'current'.");
                });
        verify(accountsRepo, never()).adjustBalance(anyLong(), any());
        verify(transactionRepo, never()).save(any());
    }

    @Test
    void currencyMismatchIs400AndChangesNothing() {
        accountExists();

        assertThatThrownBy(() -> service.purchase(purchase("1.00", "USD", null)))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(e.getReason()).isEqualTo("Account 'current' is in GBP, not USD.");
                });
        verify(accountsRepo, never()).adjustBalance(anyLong(), any());
        verify(transactionRepo, never()).save(any());
    }

    @Test
    void getAllTransactionsReturnsCurrentUsersTransactionsNewestFirst() {
        User alice = new User();
        alice.setId(9);
        when(userService.getCurrentUser()).thenReturn(alice);
        Transaction t = new Transaction(1, account, tesco, new BigDecimal("2.00"), "GBP",
                TransactionType.Purchase, LocalDateTime.now());
        when(transactionRepo.findAllByBankAccount_User_IdOrderByTimeDesc(9L)).thenReturn(List.of(t));

        List<TransactionResponse> result = service.getAllTransactions();

        assertThat(result).singleElement().satisfies(r -> {
            assertThat(r.counterparty()).isEqualTo("tesco");
            assertThat(r.value()).isEqualByComparingTo("2.00");
        });
    }

    @Test
    void getTransactionByIdThrows404WhenMissing() {
        when(transactionRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTransactionById(99))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
