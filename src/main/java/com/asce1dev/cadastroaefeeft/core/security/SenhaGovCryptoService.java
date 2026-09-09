package com.asce1dev.cadastroaefeeft.core.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class SenhaGovCryptoService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String ENVELOPE_PREFIX = "v1:";
    private static final int KEY_LENGTH_BYTES = 32;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int TAG_LENGTH_BYTES = TAG_LENGTH_BITS / Byte.SIZE;

    private final SecretKeySpec key;
    private final SecureRandom secureRandom;

    public SenhaGovCryptoService(@Value("${app.security.senha-gov.key}") String encodedKey) {
        this.key = new SecretKeySpec(decodeKey(encodedKey), ALGORITHM);
        this.secureRandom = new SecureRandom();
    }

    public String criptografar(String plaintext) {
        if (plaintext == null) {
            return null;
        }

        byte[] iv = new byte[IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] envelope = new byte[iv.length + encrypted.length];

            System.arraycopy(iv, 0, envelope, 0, iv.length);
            System.arraycopy(encrypted, 0, envelope, iv.length, encrypted.length);

            return ENVELOPE_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(envelope);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível proteger a credencial.", e);
        }
    }

    public String descriptografar(String envelope) {
        if (envelope == null) {
            return null;
        }

        try {
            if (!envelope.startsWith(ENVELOPE_PREFIX)) {
                throw new IllegalArgumentException();
            }

            byte[] decoded = Base64.getUrlDecoder().decode(envelope.substring(ENVELOPE_PREFIX.length()));
            if (decoded.length < IV_LENGTH_BYTES + TAG_LENGTH_BYTES) {
                throw new IllegalArgumentException();
            }

            byte[] iv = Arrays.copyOfRange(decoded, 0, IV_LENGTH_BYTES);
            byte[] encrypted = Arrays.copyOfRange(decoded, IV_LENGTH_BYTES, decoded.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] plaintext = cipher.doFinal(encrypted);

            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Não foi possível recuperar a credencial protegida.", e);
        }
    }

    private byte[] decodeKey(String encodedKey) {
        if (encodedKey == null || encodedKey.isBlank()) {
            throw new IllegalStateException("Chave de proteção da credencial inválida.");
        }

        try {
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            if (decodedKey.length != KEY_LENGTH_BYTES) {
                throw new IllegalStateException("Chave de proteção da credencial inválida.");
            }
            return decodedKey;
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Chave de proteção da credencial inválida.", e);
        }
    }
}
