package com.davidhorobin.budgetbalance;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class BudgetBalanceController {

    private Map<String, Transaction> transactions = new HashMap<>() {{
        put("tesco", new Transaction(100, "tesco"));
    }};

    @GetMapping("/hello")
    public String hello() {
        return "<h1>hello</h1>";
    }

    @GetMapping("/transactions")
    public Collection<Transaction> get() {
        return transactions.values();
    }

    @GetMapping("/transactions/{vendor}")
    public Transaction get(@PathVariable String vendor) {
        Transaction t = transactions.get(vendor);
        if (t == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return t;
    }

    @DeleteMapping("/transactions/{vendor}")
    public void delete(@PathVariable String vendor) {
        Transaction t = transactions.remove(vendor);
        if (t == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
