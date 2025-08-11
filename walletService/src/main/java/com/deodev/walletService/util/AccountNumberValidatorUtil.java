package com.deodev.walletService.util;

import org.springframework.stereotype.Component;

@Component
public class AccountNumberValidatorUtil {

    public boolean validateAccountNumber(String accountNumber) {
        if (accountNumber.length() != 10) {
            return false;
        }

        String nineDigits = accountNumber.substring(0,9);
        char tenthDigit = accountNumber.charAt(9);

        int generatedTenthDigit = calculateChecksum(nineDigits);
        return Character.getNumericValue(tenthDigit) == generatedTenthDigit;
    }

    private int calculateChecksum(String nineDigits) {
        int sum = 0;
        for (int i = 0; i < nineDigits.length(); i++) {
            int digit = Character.getNumericValue(nineDigits.charAt(i));
            sum += digit * (i + 1);
        }
        return sum % 10;
    }
}
