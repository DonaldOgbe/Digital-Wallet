package com.deodev.walletService.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class AccountNumberValidatorUtilTest {

    @Autowired
    private AccountNumberValidatorUtil validatorUtil;

    @BeforeEach
    void setUp() {
        this.validatorUtil = new AccountNumberValidatorUtil();
    }


    @ParameterizedTest
    @CsvSource({
            "1234567895, true",
            "1111111115, true",
            "1333393, false",
            "1111111118, false"
    })
    public void checkAccountNumberIsValid(String accountNumber, boolean expected) {
        //when
        boolean result = validatorUtil.validateAccountNumber(accountNumber);

        //then
        assertEquals(expected, result);
    }
}