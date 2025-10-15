package com.deodev.transactionService.pspService.flutterwave.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class EncryptorService {

    @Value("${psp.flutterwave.encryption-key}")
    private final String key24;

    public String encrypt3DES(String plainJson) throws Exception {
        byte[] keyBytes = key24.getBytes("UTF-8");
        SecretKeySpec key = new SecretKeySpec(keyBytes, "DESede");

        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(plainJson.getBytes("UTF-8"));

        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decrypt3DES(String base64CipherText) throws Exception {
        byte[] keyBytes = key24.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 24) {
            throw new IllegalArgumentException("Key must be 24 bytes for 3DES (got " + keyBytes.length + ")");
        }
        SecretKeySpec key = new SecretKeySpec(keyBytes, "DESede");

        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decoded = Base64.getDecoder().decode(base64CipherText);
        byte[] decrypted = cipher.doFinal(decoded);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

}
