package com.davidhorobin.budgetbalance.controller;

import com.davidhorobin.budgetbalance.dto.accounts.CreateRequest;
import com.davidhorobin.budgetbalance.dto.accounts.CreateResponse;
import com.davidhorobin.budgetbalance.dto.accounts.DepositRequest;
import com.davidhorobin.budgetbalance.dto.accounts.DepositResponse;
import com.davidhorobin.budgetbalance.service.AccountsService;
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

    @PostMapping("/new")
    public ResponseEntity<CreateResponse> createAccount(@Valid @RequestBody CreateRequest request) {
        return ResponseEntity.ok().body(accountsService.createAccount(request));
    }

    @PatchMapping("/deposit")
    public ResponseEntity<DepositResponse> deposit(@Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok().body(accountsService.deposit(request));
    }
}
