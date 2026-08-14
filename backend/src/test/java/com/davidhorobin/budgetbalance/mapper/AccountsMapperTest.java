package com.davidhorobin.budgetbalance.mapper;

import com.davidhorobin.budgetbalance.dto.accounts.CreateRequest;
import com.davidhorobin.budgetbalance.dto.accounts.CreateResponse;
import com.davidhorobin.budgetbalance.entity.BankAccount;
import com.davidhorobin.budgetbalance.entity.Counterparty;
import com.davidhorobin.budgetbalance.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AccountsMapperTest {
    @Test
    void toEntity_mapsAllFields() {
        CreateRequest request = new CreateRequest("Current", "HSBC", BigDecimal.valueOf(150), "GBP");
        String name = "current";
        User user = new User();
        Counterparty counterparty = new Counterparty();

        BankAccount a = AccountsMapper.toEntity(request, name, user, counterparty);

        assertEquals("current", a.getName());
        assertSame(user, a.getUser());
        assertSame(counterparty, a.getCounterparty());
        assertEquals(BigDecimal.valueOf(150), a.getBalance());
        assertEquals("GBP", a.getCurrency());
    }

    @Test
    void toResponse_mapsAllFields() {
        CreateResponse result = AccountsMapper.toResponse("current", BigDecimal.valueOf(100.25), "NatWest");

        assertEquals("current", result.name());
        assertEquals(BigDecimal.valueOf(100.25), result.balance());
        assertEquals("NatWest", result.bank());
    }
}
