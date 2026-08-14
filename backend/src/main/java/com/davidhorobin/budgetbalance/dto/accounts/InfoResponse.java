package com.davidhorobin.budgetbalance.dto.accounts;

import java.math.BigDecimal;
import java.util.List;

public record InfoResponse(
        BigDecimal sum,
        List<AccountInfo> accounts
) {
}
