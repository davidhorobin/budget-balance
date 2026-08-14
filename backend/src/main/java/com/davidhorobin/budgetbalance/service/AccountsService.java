package com.davidhorobin.budgetbalance.service;

import com.davidhorobin.budgetbalance.dto.accounts.AccountInfo;
import com.davidhorobin.budgetbalance.dto.accounts.CreateRequest;
import com.davidhorobin.budgetbalance.dto.accounts.CreateResponse;
import com.davidhorobin.budgetbalance.dto.accounts.InfoResponse;
import com.davidhorobin.budgetbalance.entity.BankAccount;
import com.davidhorobin.budgetbalance.entity.Counterparty;
import com.davidhorobin.budgetbalance.entity.User;
import com.davidhorobin.budgetbalance.mapper.AccountsMapper;
import com.davidhorobin.budgetbalance.repository.AccountsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountsService {
    private final AccountsRepo accountsRepo;
    private final UserService userService;
    private final CounterpartyService counterpartyService;

    public CreateResponse createAccount(CreateRequest request) {
        User user = userService.getCurrentUser();
        Counterparty counterparty = counterpartyService.resolveOrCreateCounterparty(request.bank());
        String normalisedName = request.name().trim().toLowerCase();
        BankAccount account = AccountsMapper.toEntity(request, normalisedName, user, counterparty);
        BankAccount saved = accountsRepo.save(account);

        String name = saved.getName();
        BigDecimal balance = saved.getBalance();
        String bank = saved.getCounterparty().getName();
        return AccountsMapper.toResponse(name, balance, bank);
    }

    public InfoResponse getAllAccounts() {
        User user = userService.getCurrentUser();
        List<BankAccount> accounts = accountsRepo.findAllByUserId(user.getId());
        List<AccountInfo> accountsInfo = new ArrayList<>();
        BigDecimal sum = BigDecimal.ZERO;
        for (BankAccount account : accounts) {
            sum = sum.add(account.getBalance());
            accountsInfo.add(AccountsMapper.toAccountInfo(account));
        }
        return new InfoResponse(sum, accountsInfo);
    }

    public BankAccount resolveBankAccount(String name) {
        String normalisedName = name.trim().toLowerCase();
        User currentUser = userService.getCurrentUser();
        return accountsRepo.findByNameAndUserId(normalisedName, currentUser.getId())
                .orElse(null);
    }
}
