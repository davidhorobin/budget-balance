package com.davidhorobin.budgetbalance.mapper;

import com.davidhorobin.budgetbalance.dto.transaction.TransactionRequest;
import com.davidhorobin.budgetbalance.dto.transaction.TransactionResponse;
import com.davidhorobin.budgetbalance.entity.Transaction;

public class TransactionMapper {

    public static Transaction toEntity(TransactionRequest req) {
        Transaction t = new Transaction();
        t.setAmount(req.amount());
        if (req.time() != null) {
            t.setTime(req.time());
        }
        return t;
    }

    public static TransactionResponse toResponse(Transaction t) {
        String name = null;
        if (t.getCounterparty() != null) name = t.getCounterparty().getName();

        return new TransactionResponse(
                t.getId(),
                t.getAmount(),
                name,
                t.getTime()
        );
    }
}
