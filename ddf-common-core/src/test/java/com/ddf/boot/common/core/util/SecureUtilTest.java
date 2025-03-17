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
        Assertions.assertEquals(data, SecureUtil.decryptFromHexByAES(SecureUtil.encryptHexByAES(data)));

        String aesSecret = "ggwegweegwegwegwegwegqasfsafweqe";
        Assertions.assertEquals(data, SecureUtil.decryptFromHexByAESWithKey(SecureUtil.encryptHexByAESWithKey(data, aesSecret), aesSecret));
    }

    @Test
    public void testRsa() {
        String data = "snowball";
        Assertions.assertEquals(data, SecureUtil.localPublicDecryptFromBcd(SecureUtil.localPrivateEncryptBcd(data)));
    }


    @Test
    public void testOthers() {
        String data = "snowball";
        String secret = "ggwegweegwegwegwegwegqasfsafweqe";
        SecureUtil.signWithHMac("snowball", secret);
        SecureUtil.bCryptMatch(data, SecureUtil.bCryptEncoder(data));
    }
}
