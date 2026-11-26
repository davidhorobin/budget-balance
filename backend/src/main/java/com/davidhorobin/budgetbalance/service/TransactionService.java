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
import com.davidhorobin.budgetbalance.mapper.AccountsMapper;
import com.davidhorobin.budgetbalance.mapper.TransactionMapper;
import com.davidhorobin.budgetbalance.repository.AccountsRepo;
import com.davidhorobin.budgetbalance.repository.TransactionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final TransactionRepo transactionRepo;
    private final CounterpartyService counterpartyService;
    private final AccountsRepo accountsRepo;
    private final AccountsService accountsService;
    private final UserService userService;

    public List<TransactionResponse> getAllTransactions() {
        User user = userService.getCurrentUser();
        return transactionRepo.findAllByBankAccount_User_IdOrderByTimeDesc(user.getId()).stream()
                .map(TransactionMapper::toResponse)
                .toList();
    }

    public TransactionResponse getTransactionById(long id) {
        Transaction transaction = transactionRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Transaction with id " + id + " not found."
                ));
        return TransactionMapper.toResponse(transaction);
    }

    private TransactionResponse saveTransaction(TransactionRequest request, TransactionType type) {
        BankAccount account = accountsService.resolveBankAccount(request.bankAccount());
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No account named '%s'.".formatted(request.bankAccount()));
        }
        if (!account.getCurrency().equals(request.currency())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Account '%s' is in %s, not %s.".formatted(account.getName(), account.getCurrency(), request.currency()));
        }
        Counterparty counterparty = counterpartyService.resolveOrCreateCounterparty(request.counterparty());
        LocalDateTime time = LocalDateTime.now();
        if (request.time() != null) time = request.time();

        // Purchases may take the balance below zero (overdraft, no limit).
        if (type == TransactionType.Deposit) {
            accountsRepo.adjustBalance(account.getId(), request.amount());
        } else if (type == TransactionType.Purchase) {
            accountsRepo.adjustBalance(account.getId(), request.amount().negate());
        }
        Transaction transaction = TransactionMapper.toEntity(request, account, counterparty, type, time);
        Transaction saved = transactionRepo.save(transaction);
        log.info("Transaction with id {} saved", saved.getId());
        return TransactionMapper.toResponse(saved);
    }

    // Balance update and transaction insert commit or roll back together.
    @Transactional
    public DepositResponse deposit(DepositRequest request) {
        return AccountsMapper.toDepositResponse(
                saveTransaction(
                        AccountsMapper.toTransactionRequest(request), TransactionType.Deposit
                )
        );
    }

    @Transactional
    public TransactionResponse purchase(TransactionRequest request) {
        return saveTransaction(request, TransactionType.Purchase);
    }

    public void deleteTransactionById(long id) {
        transactionRepo.deleteById(id);
    }

}
