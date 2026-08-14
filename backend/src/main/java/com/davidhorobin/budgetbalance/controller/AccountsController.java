package com.davidhorobin.budgetbalance.controller;

import com.davidhorobin.budgetbalance.dto.accounts.*;
import com.davidhorobin.budgetbalance.service.AccountsService;
import com.davidhorobin.budgetbalance.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Validated
public class AccountsController {
    private final AccountsService accountsService;
    private final TransactionService transactionService;

    @PostMapping("/new")
    public ResponseEntity<CreateResponse> createAccount(@Valid @RequestBody CreateRequest request) {
        return ResponseEntity.ok().body(accountsService.createAccount(request));
    }

    @GetMapping("/info")
    public ResponseEntity<InfoResponse> getAccountInfo() {
        return ResponseEntity.ok().body(accountsService.getAllAccounts());
    }

    @PatchMapping("/deposit")
    public ResponseEntity<DepositResponse> deposit(@Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok().body(transactionService.deposit(request));
    }
}
