package com.deodev.walletService.accountService.repository;


import com.deodev.walletService.accountService.model.Account;
import com.deodev.walletService.enums.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository testAccountRepository;

    @Test
    public void testItWorks() {
        // given
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String accountNumber = "1234567890";

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .walletId(walletId)
                .userId(userId)
                .currency(Currency.NGN)
                .build();

        // when
        testAccountRepository.save(account);

        // then
        assertThat(testAccountRepository.existsByWalletId(walletId)).isTrue();
    }

}