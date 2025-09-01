package com.deodev.walletService.accountService.dto.response;

import com.deodev.walletService.accountService.model.Account;
import lombok.Builder;

import java.util.List;

@Builder
public record GetUserAccountsResponse(
        List<Account> accounts
) {
}
