package com.deodev.transactionService.pspService.flutterwave.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class EncryptorServiceTest {

    private EncryptorService encryptionService;
    private String key24;
    private String plainText;

    @BeforeEach
    void setUp() {
        key24 = "123456789012345678901234";
        encryptionService = new EncryptorService(key24);
        plainText = "{\"amount\":100,\"currency\":\"NGN\"}";
    }

    @Test
    void encrypt3DES_shouldReturnBase64String_whenInputIsValid() throws Exception {
        // when
        String encrypted = encryptionService.encrypt3DES(plainText);

        // then
        assertThat(encrypted).isNotNull();
        assertThat(Base64.getDecoder().decode(encrypted)).isNotEmpty();
    }

    @Test
    void decrypt3DES_shouldReturnOriginalPlainText_whenEncryptedTextIsValid() throws Exception {
        // given
        String encrypted = encryptionService.encrypt3DES(plainText);

        // when
        String decrypted = encryptionService.decrypt3DES(encrypted);

        // then
        assertThat(decrypted).isEqualTo(plainText);
    }
}