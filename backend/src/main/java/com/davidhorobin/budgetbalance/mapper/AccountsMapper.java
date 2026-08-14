package com.davidhorobin.budgetbalance.mapper;

import com.davidhorobin.budgetbalance.dto.accounts.*;
import com.davidhorobin.budgetbalance.dto.transaction.TransactionRequest;
import com.davidhorobin.budgetbalance.dto.transaction.TransactionResponse;
import com.davidhorobin.budgetbalance.entity.BankAccount;
import com.davidhorobin.budgetbalance.entity.Counterparty;
import com.davidhorobin.budgetbalance.entity.User;

import java.math.BigDecimal;

public class AccountsMapper {
    public static BankAccount toEntity(CreateRequest request, String name, User user, Counterparty counterparty) {
        BankAccount a = new BankAccount();
        a.setName(name);
        a.setUser(user);
        a.setCounterparty(counterparty);
        a.setBalance(request.balance());
        a.setCurrency(request.currency());
        return a;
    }

    public static CreateResponse toResponse(String name, BigDecimal balance, String bank) {
        return new CreateResponse(name, balance, bank);
    }

    public static TransactionRequest toTransactionRequest(DepositRequest request) {
        return new TransactionRequest(
                request.accountName(), "Deposit", request.amount(), request.currency(), request.time());
    }

    public static DepositResponse toDepositResponse(TransactionResponse response) {
        return new DepositResponse(response.counterparty(), response.value(), response.time());
    }

    public static AccountInfo toAccountInfo(BankAccount account) {
        return new AccountInfo(
                account.getName(), account.getCounterparty().getName(), account.getBalance(), account.getCurrency()
        );
    }
}
