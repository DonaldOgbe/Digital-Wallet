package com.deodev.walletService.util;

import org.springframework.stereotype.Component;

@Component
public class AccountNumberGenerator {


    private String generateRandomBaseNumber() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < 9; i++) {
            int num = (int) (Math.random() * 10);
            stringBuilder.append(num);
        }

        return stringBuilder.toString();
    }

    private int calculateChecksum(String nineDigits) {
        int sum = 0;
        for (int i = 0; i < nineDigits.length(); i++) {
            int digit = Character.getNumericValue(nineDigits.charAt(i));
            sum += digit * (i + 1);
        }
        return sum % 10;
    }

    public String generateAccountNumber() {
        String baseNumber = generateRandomBaseNumber();
        int tenthDigit = calculateChecksum(baseNumber);

        return baseNumber.concat(String.valueOf(tenthDigit));
    }
}
