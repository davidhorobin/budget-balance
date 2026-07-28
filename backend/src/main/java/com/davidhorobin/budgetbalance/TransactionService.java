package com.davidhorobin.budgetbalance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final TransactionRepo transactionRepo;

    public List<Transaction> getAllTransactions() {
        return transactionRepo.findAll();
    }

    public Transaction getTransactionById(Integer id) {
        Optional<Transaction> transaction = transactionRepo.findById(id);
        if (transaction.isPresent()) {
            return transaction.get();
        }
        log.info("Transaction with id {} not found", id);
        return null;
    }

    public Transaction saveTransaction(Transaction transaction) {
        transaction.setTime(LocalDateTime.now());
        Transaction savedTransaction = transactionRepo.save(transaction);
        log.info("Transaction with id {} saved", savedTransaction.getId());
        return savedTransaction;
    }

    public void deleteTransactionById(Integer id) {
        transactionRepo.deleteById(id);
    }
}
