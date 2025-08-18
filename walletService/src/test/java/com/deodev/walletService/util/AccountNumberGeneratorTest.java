package com.deodev.walletService.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccountNumberGeneratorTest {

    private AccountNumberGenerator accountNumberGenerator;
    private AccountNumberValidatorUtil validatorUtil;

    @BeforeEach
    void setUp() {
        this.accountNumberGenerator = new AccountNumberGenerator();
        this.validatorUtil = new AccountNumberValidatorUtil();
    }


    @Test
    public void generateValidTenDigitAccountNumber() {
        // given
        String accountNumber =  accountNumberGenerator.generateAccountNumber();

        // when
        boolean result = validatorUtil.validateAccountNumber(accountNumber);

        // then
        assertTrue(result);
    }

}