package com.davidhorobin.budgetbalance.service;

import com.davidhorobin.budgetbalance.dto.transaction.TransactionRequest;
import com.davidhorobin.budgetbalance.dto.transaction.TransactionResponse;
import com.davidhorobin.budgetbalance.entity.Transaction;
import com.davidhorobin.budgetbalance.mapper.TransactionMapper;
import com.davidhorobin.budgetbalance.repository.TransactionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final TransactionRepo transactionRepo;

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepo.findAll().stream()
                .map(TransactionMapper::toResponse)
                .toList();
    }

    public TransactionResponse getTransactionById(Integer id) {
        Transaction transaction = transactionRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Transaction with id " + id + " not found."
                ));
        return TransactionMapper.toResponse(transaction);
    }

    public TransactionResponse saveTransaction(TransactionRequest request) {
        Transaction t = TransactionMapper.toEntity(request);
        if (t.getTime() == null) t.setTime(LocalDateTime.now());
        Transaction saved = transactionRepo.save(t);
        log.info("Transaction with id {} saved", saved.getId());
        return TransactionMapper.toResponse(saved);
    }

    public void deleteTransactionById(Integer id) {
        transactionRepo.deleteById(id);
    }
}
