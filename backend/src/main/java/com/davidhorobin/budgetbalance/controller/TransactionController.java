package com.davidhorobin.budgetbalance.controller;

import com.davidhorobin.budgetbalance.service.TransactionService;
import com.davidhorobin.budgetbalance.entity.Transaction;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok().body(transactionService.getAllTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Integer id) {
        return ResponseEntity.ok().body(transactionService.getTransactionById(id));
    }

    @PostMapping("/")
    public ResponseEntity<Transaction> saveTransaction(@Valid @RequestBody Transaction transaction) {
        return ResponseEntity.ok().body(transactionService.saveTransaction(transaction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransactionById(@PathVariable Integer id) {
        transactionService.deleteTransactionById(id);
        return ResponseEntity.ok().body("Deleted transaction successfully");
    }

}
