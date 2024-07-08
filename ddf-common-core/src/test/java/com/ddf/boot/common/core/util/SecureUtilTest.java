package com.ddf.boot.common.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2024/07/08 14:07
 */
public class SecureUtilTest {


    @Test
    public void testAes() {
        String data = "snowball";
        Assertions.assertEquals(data, SecureUtils.aesDecryptStr(SecureUtils.aesEncryptHex(data)));
        Assertions.assertEquals(data, SecureUtils.aesDecryptStr(SecureUtils.aesEncryptBase64(data)));

        String aesSecret = "ggwegweegwegwegwegwegqasfsafweqe";
        Assertions.assertEquals(data, SecureUtils.aesDecryptStr(SecureUtils.aesEncryptHex(data, aesSecret), aesSecret));
        Assertions.assertEquals(data, SecureUtils.aesDecryptStr(SecureUtils.aesEncryptBase64(data, aesSecret), aesSecret));
    }

    @Test
    public void testRsa() {
        String data = "snowball";
        Assertions.assertEquals(data, SecureUtils.rsaPublicDecryptStr(SecureUtils.rsaPrivateEncryptHex(data)));
        Assertions.assertEquals(data, SecureUtils.rsaPublicDecryptStr(SecureUtils.rsaPrivateEncryptBase64(data)));
        Assertions.assertEquals(data, SecureUtils.rsaPrivateDecryptStr(SecureUtils.rsaPublicEncryptHex(data)));
        Assertions.assertEquals(data, SecureUtils.rsaPrivateDecryptStr(SecureUtils.rsaPublicEncryptBase64(data)));
    }


    @Test
    public void testOthers() {
        String data = "snowball";
        String secret = "ggwegweegwegwegwegwegqasfsafweqe";
        SecureUtils.signWithHMac("snowball", secret);
        SecureUtils.bCryptMatch(data, SecureUtils.bCryptEncoder(data));
    }
}
