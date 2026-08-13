package com.davidhorobin.budgetbalance.mapper;

import com.davidhorobin.budgetbalance.dto.accounts.CreateRequest;
import com.davidhorobin.budgetbalance.dto.accounts.CreateResponse;
import com.davidhorobin.budgetbalance.entity.BankAccount;
import com.davidhorobin.budgetbalance.entity.Counterparty;
import com.davidhorobin.budgetbalance.entity.User;

import java.math.BigDecimal;

public class AccountsMapper {
    public static BankAccount toEntity(CreateRequest request, User user, Counterparty counterparty) {
        BankAccount a = new BankAccount();
        a.setName(request.name());
        a.setUser(user);
        a.setCounterparty(counterparty);
        a.setBalance(request.balance());
        a.setCurrency(request.currency());
        return a;
    }

    public static CreateResponse toResponse(String name, BigDecimal balance, String bank) {
        return new CreateResponse(name, balance, bank);
    }
}
