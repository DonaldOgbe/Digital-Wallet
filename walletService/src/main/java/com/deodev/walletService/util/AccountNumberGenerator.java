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
}
