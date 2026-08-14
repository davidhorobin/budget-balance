package com.davidhorobin.budgetbalance.service;

import com.davidhorobin.budgetbalance.dto.accounts.DepositRequest;
import com.davidhorobin.budgetbalance.dto.accounts.DepositResponse;
import com.davidhorobin.budgetbalance.dto.transaction.TransactionRequest;
import com.davidhorobin.budgetbalance.dto.transaction.TransactionResponse;
import com.davidhorobin.budgetbalance.entity.BankAccount;
import com.davidhorobin.budgetbalance.entity.Counterparty;
import com.davidhorobin.budgetbalance.entity.Transaction;
import com.davidhorobin.budgetbalance.enums.TransactionType;
import com.davidhorobin.budgetbalance.mapper.AccountsMapper;
import com.davidhorobin.budgetbalance.mapper.TransactionMapper;
import com.davidhorobin.budgetbalance.repository.AccountsRepo;
import com.davidhorobin.budgetbalance.repository.CounterpartyRepo;
import com.davidhorobin.budgetbalance.repository.TransactionRepo;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
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

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepo.findAllByOrderByTimeAsc().stream()
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
        if (account == null) return null;
        Counterparty counterparty = counterpartyService.resolveOrCreateCounterparty(request.counterparty());
        LocalDateTime time = LocalDateTime.now();
        if (request.time() != null) time = request.time();

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

    public DepositResponse deposit(DepositRequest request) {
        return AccountsMapper.toDepositResponse(
                saveTransaction(
                        AccountsMapper.toTransactionRequest(request), TransactionType.Deposit
                )
        );
    }

    public TransactionResponse purchase(TransactionRequest request) {
        return saveTransaction(request, TransactionType.Purchase);
    }

    public void deleteTransactionById(long id) {
        transactionRepo.deleteById(id);
    }

}
