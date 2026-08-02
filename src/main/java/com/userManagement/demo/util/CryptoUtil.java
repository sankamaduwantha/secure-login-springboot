package com.userManagement.demo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class CryptoUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;  
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public CryptoUtil(@Value("${app.security.token.secret-key}") String secretKeyBase64) {
        byte[] secretKeyBytes = Base64.getDecoder().decode(secretKeyBase64);
        this.secretKey = new SecretKeySpec(secretKeyBytes, "AES");
    }

    public String encrypt(String plainText) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

        byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"));

        ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
        buffer.put(iv);
        buffer.put(cipherText);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
    }

    public String decrypt(String rawToken) throws Exception {
        byte[] decoded = Base64.getUrlDecoder().decode(rawToken);

        ByteBuffer buffer = ByteBuffer.wrap(decoded);
        byte[] iv = new byte[GCM_IV_LENGTH];
        buffer.get(iv);
        byte[] cipherText = new byte[buffer.remaining()];
        buffer.get(cipherText);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText, "UTF-8");
    }
}