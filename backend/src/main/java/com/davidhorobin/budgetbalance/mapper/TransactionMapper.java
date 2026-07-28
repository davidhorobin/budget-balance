package com.davidhorobin.budgetbalance.mapper;

import com.davidhorobin.budgetbalance.dto.transaction.TransactionRequest;
import com.davidhorobin.budgetbalance.dto.transaction.TransactionResponse;
import com.davidhorobin.budgetbalance.entity.Transaction;

public class TransactionMapper {

    public static Transaction toEntity(TransactionRequest req) {
        Transaction t = new Transaction();
        t.setAmount(req.amount());
        t.setCounterparty(req.counterparty());
        if (req.time() != null) {
            t.setTime(req.time());
        }
        return t;
    }

    public static TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getAmount(),
                t.getCounterparty(),
                t.getTime()
        );
    }
}
