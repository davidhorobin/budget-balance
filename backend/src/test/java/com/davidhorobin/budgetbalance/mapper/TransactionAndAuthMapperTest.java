package com.davidhorobin.budgetbalance.mapper;

import com.davidhorobin.budgetbalance.dto.auth.RegisterRequest;
import com.davidhorobin.budgetbalance.dto.transaction.TransactionRequest;
import com.davidhorobin.budgetbalance.dto.transaction.TransactionResponse;
import com.davidhorobin.budgetbalance.entity.BankAccount;
import com.davidhorobin.budgetbalance.entity.Counterparty;
import com.davidhorobin.budgetbalance.entity.Transaction;
import com.davidhorobin.budgetbalance.entity.User;
import com.davidhorobin.budgetbalance.enums.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionAndAuthMapperTest {

    @Test
    void transactionToEntityMapsAllFields() {
        BankAccount account = new BankAccount();
        Counterparty tesco = new Counterparty(1, "tesco");
        LocalDateTime time = LocalDateTime.now();
        TransactionRequest req = new TransactionRequest("current", "Tesco", new BigDecimal("9.99"), "GBP", null);

        Transaction t = TransactionMapper.toEntity(req, account, tesco, TransactionType.Purchase, time);

        assertThat(t.getBankAccount()).isSameAs(account);
        assertThat(t.getCounterparty()).isSameAs(tesco);
        assertThat(t.getAmount()).isEqualByComparingTo("9.99");
        assertThat(t.getCurrency()).isEqualTo("GBP");
        assertThat(t.getType()).isEqualTo(TransactionType.Purchase);
        assertThat(t.getTime()).isEqualTo(time);
    }

    @Test
    void transactionToResponseMapsAllFields() {
        LocalDateTime time = LocalDateTime.now();
        Transaction t = new Transaction(1, null, new Counterparty(1, "tesco"), new BigDecimal("9.99"), "GBP",
                TransactionType.Purchase, time);

        TransactionResponse r = TransactionMapper.toResponse(t);

        assertThat(r.value()).isEqualByComparingTo("9.99");
        assertThat(r.counterparty()).isEqualTo("tesco");
        assertThat(r.time()).isEqualTo(time);
        assertThat(r.type()).isEqualTo(TransactionType.Purchase);
    }

    @Test
    void transactionToResponseToleratesMissingCounterparty() {
        Transaction t = new Transaction();
        assertThat(TransactionMapper.toResponse(t).counterparty()).isNull();
    }

    @Test
    void registerRequestToUser() {
        User u = AuthMapper.toEntity(new RegisterRequest("alice", "pw", "alice@example.com"));

        assertThat(u.getUsername()).isEqualTo("alice");
        assertThat(u.getPassword()).isEqualTo("pw");
        assertThat(u.getEmail()).isEqualTo("alice@example.com");
    }
}
