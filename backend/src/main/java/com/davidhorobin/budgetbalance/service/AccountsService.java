package com.davidhorobin.budgetbalance.service;

import com.davidhorobin.budgetbalance.dto.accounts.CreateRequest;
import com.davidhorobin.budgetbalance.dto.accounts.CreateResponse;
import com.davidhorobin.budgetbalance.dto.accounts.DepositRequest;
import com.davidhorobin.budgetbalance.dto.accounts.DepositResponse;
import com.davidhorobin.budgetbalance.dto.transaction.TransactionRequest;
import com.davidhorobin.budgetbalance.entity.BankAccount;
import com.davidhorobin.budgetbalance.entity.Counterparty;
import com.davidhorobin.budgetbalance.entity.User;
import com.davidhorobin.budgetbalance.enums.TransactionType;
import com.davidhorobin.budgetbalance.mapper.AccountsMapper;
import com.davidhorobin.budgetbalance.mapper.TransactionMapper;
import com.davidhorobin.budgetbalance.repository.AccountsRepo;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountsService {
    private final AccountsRepo accountsRepo;
    private final UserService userService;
    private final CounterpartyService counterpartyService;
    private final TransactionService transactionService;

    public CreateResponse createAccount(CreateRequest request) {
        User user = userService.getCurrentUser();
        Counterparty counterparty = counterpartyService.resolveOrCreateCounterparty(request.bank());
        BankAccount account = AccountsMapper.toEntity(request, user, counterparty);
        BankAccount saved = accountsRepo.save(account);

        String name = saved.getName();
        BigDecimal balance = saved.getBalance();
        String bank = saved.getCounterparty().getName();
        return AccountsMapper.toResponse(name, balance, bank);
    }

    public DepositResponse deposit(DepositRequest request) {
        return AccountsMapper.toDepositResponse(
                transactionService.saveTransaction(
                        AccountsMapper.toTransactionRequest(request), TransactionType.Deposit
                )
        );
    }
}
