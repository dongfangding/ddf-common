package com.ddf.boot.common.websocket.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import cn.hutool.crypto.asymmetric.RSA;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * WsSecureUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class WsSecureUtilTest {

    @BeforeAll
    static void initRsa() {
        WsSecureUtil.setRsaForTest(new RSA());
    }

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

        assertEquals("b82fcb791acec57859b989b430a826488ce2e479fdf92326bd0a2e8375a42ba4", sign1);
        assertEquals(sign1, sign2);
    }
}
