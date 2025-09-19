package com.deodev.walletService.accountService.dto.response;

import com.deodev.walletService.accountService.model.Account;
import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.util.List;

@Builder
public record GetUserAccountsResponse(
        List<Account> accounts
) {
}
