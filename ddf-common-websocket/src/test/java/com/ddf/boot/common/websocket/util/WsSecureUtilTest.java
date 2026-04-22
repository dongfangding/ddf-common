package com.ddf.boot.common.websocket.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * WsSecureUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class WsSecureUtilTest {

    @Test
    @DisplayName("应支持公钥加密私钥解密")
    void shouldEncryptByPublicKeyAndDecryptByPrivateKey() {
        String plainText = "{\"userId\":\"1001\",\"name\":\"测试用户\"}";

        String encrypted = WsSecureUtil.publicEncryptBcd(plainText);
        String decrypted = WsSecureUtil.privateDecryptFromBcd(encrypted);

        assertFalse(encrypted.isEmpty());
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("应支持私钥加密公钥解密")
    void shouldEncryptByPrivateKeyAndDecryptByPublicKey() {
        String plainText = "handshake-token";

        String encrypted = WsSecureUtil.privateEncryptBcd(plainText);
        String decrypted = WsSecureUtil.publicDecryptFromBcd(encrypted);

        assertFalse(encrypted.isEmpty());
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("HMAC 签名结果应稳定")
    void shouldGenerateStableHmacSignature() {
        String sign1 = WsSecureUtil.signWithHMac("payload", "secret");
        String sign2 = WsSecureUtil.signWithHMac("payload", "secret");

        assertEquals("4f967408b96fb0e40180074062022698", sign1);
        assertEquals(sign1, sign2);
    }
}
