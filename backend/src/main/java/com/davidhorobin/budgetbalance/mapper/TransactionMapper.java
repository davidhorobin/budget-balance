package com.davidhorobin.budgetbalance.mapper;

import com.davidhorobin.budgetbalance.dto.transaction.TransactionRequest;
import com.davidhorobin.budgetbalance.dto.transaction.TransactionResponse;
import com.davidhorobin.budgetbalance.entity.BankAccount;
import com.davidhorobin.budgetbalance.entity.Counterparty;
import com.davidhorobin.budgetbalance.entity.Transaction;
import com.davidhorobin.budgetbalance.enums.TransactionType;

import java.time.LocalDateTime;

public class TransactionMapper {

    public static Transaction toEntity(
            TransactionRequest req,
            BankAccount account,
            Counterparty counterparty,
            TransactionType type,
            LocalDateTime time
    ) {
        Transaction t = new Transaction();
        t.setBankAccount(account);
        t.setCounterparty(counterparty);
        t.setAmount(req.amount());
        t.setCurrency(req.currency());
        t.setType(type);
        t.setTime(time);
        return t;
    }

    public static TransactionResponse toResponse(Transaction t) {
        String name = null;
        if (t.getCounterparty() != null) name = t.getCounterparty().getName();

        return new TransactionResponse(
                t.getAmount(),
                name,
                t.getTime()
        );
    }
}
