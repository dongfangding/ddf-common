package com.ddf.boot.common.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * SecureUtil 测试类
 * 注意：此类需要 Spring Context 和 GlobalProperties 配置才能运行
 *
 * @author Snowball
 * @version 1.0
 * @since 2024/07/08 14:07
 */
@Disabled("需要 Spring Context 和 GlobalProperties 配置")
public class SecureUtilTest {


    @Test
    public void testAes() {
        String data = "snowball";
        Assertions.assertEquals(data, SecureUtil.aesDecryptStr(SecureUtil.aesEncryptHex(data)));

        String aesSecret = "ggwegweegwegwegwegwegqasfsafweqe";
        Assertions.assertEquals(data,
                SecureUtil.aesDecryptStrWithKey(SecureUtil.aesEncryptHexWithKey(data, aesSecret), aesSecret));
    }


    @Test
    public void testOthers() {
        String data = "snowball";
        String secret = "ggwegweegwegwegwegwegqasfsafweqe";
        SecureUtil.signWithHMac("snowball", secret);
        SecureUtil.bCryptMatch(data, SecureUtil.bCryptEncoder(data));
    }
}
