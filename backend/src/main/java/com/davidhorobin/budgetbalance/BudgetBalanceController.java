package com.davidhorobin.budgetbalance;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
public class BudgetBalanceController {

    private Map<String, Transaction> transactions = new HashMap<>() {{
        UUID id = UUID.randomUUID();
        put(id.toString(), new Transaction(id.toString(), 100, "tesco"));
    }};

    @GetMapping("/hello")
    public String hello() {
        return "<h1>hello</h1>";
    }

    @GetMapping("/transactions")
    public Collection<Transaction> get() {
        return transactions.values();
    }

    @GetMapping("/transactions/{id}")
    public Transaction get(@PathVariable int id) {
        Transaction t = transactions.get(id);
        if (t == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return t;
    }

    @DeleteMapping("/transactions/{id}")
    public void delete(@PathVariable int id) {
        Transaction t = transactions.remove(id);
        if (t == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/transactions")
    public Transaction post(@RequestBody @Valid Transaction t) {
        t.setId(UUID.randomUUID().toString());
        transactions.put(t.getId(), t);
        return t;
    }
}
