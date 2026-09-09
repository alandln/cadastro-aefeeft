package com.asce1dev.cadastroaefeeft.core.security;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SenhaGovCryptoServiceTest {

    private static final String PLAINTEXT = "Credencial Gov.br segura";

    @Test
    void deve_criptografar_e_descriptografar_recuperando_valor_original() {
        SenhaGovCryptoService service = new SenhaGovCryptoService(encodedKey(1));

        String encrypted = service.criptografar(PLAINTEXT);

        assertEquals(PLAINTEXT, service.descriptografar(encrypted));
    }

    @Test
    void deve_gerar_ciphertexts_diferentes_para_o_mesmo_plaintext() {
        SenhaGovCryptoService service = new SenhaGovCryptoService(encodedKey(1));

        String first = service.criptografar(PLAINTEXT);
        String second = service.criptografar(PLAINTEXT);

        assertNotEquals(first, second);
    }

    @Test
    void deve_gerar_envelope_com_versao_v1() {
        SenhaGovCryptoService service = new SenhaGovCryptoService(encodedKey(1));

        assertTrue(service.criptografar(PLAINTEXT).startsWith("v1:"));
    }

    @Test
    void deve_rejeitar_ciphertext_adulterado() {
        SenhaGovCryptoService service = new SenhaGovCryptoService(encodedKey(1));
        String encrypted = service.criptografar(PLAINTEXT);
        String tampered = tamper(encrypted);

        assertThrows(IllegalStateException.class, () -> service.descriptografar(tampered));
    }

    @Test
    void chave_diferente_nao_deve_descriptografar() {
        SenhaGovCryptoService encryptor = new SenhaGovCryptoService(encodedKey(1));
        SenhaGovCryptoService decryptor = new SenhaGovCryptoService(encodedKey(2));
        String encrypted = encryptor.criptografar(PLAINTEXT);

        assertThrows(IllegalStateException.class, () -> decryptor.descriptografar(encrypted));
    }

    @Test
    void deve_rejeitar_chave_invalida() {
        assertThrows(IllegalStateException.class, () -> new SenhaGovCryptoService("chave-invalida"));
        assertThrows(IllegalStateException.class, () -> new SenhaGovCryptoService(encodedKeyWithLength(31)));
    }

    @Test
    void deve_manter_null_na_criptografia_e_descriptografia() {
        SenhaGovCryptoService service = new SenhaGovCryptoService(encodedKey(1));

        assertNull(service.criptografar(null));
        assertNull(service.descriptografar(null));
    }

    private String tamper(String envelope) {
        String encoded = envelope.substring("v1:".length());
        byte[] decoded = Base64.getUrlDecoder().decode(encoded);
        decoded[decoded.length - 1] ^= 1;
        return "v1:" + Base64.getUrlEncoder().withoutPadding().encodeToString(decoded);
    }

    private String encodedKey(int value) {
        byte[] key = new byte[32];
        key[0] = (byte) value;
        return Base64.getEncoder().encodeToString(key);
    }

    private String encodedKeyWithLength(int length) {
        return Base64.getEncoder().encodeToString(new byte[length]);
    }
}
