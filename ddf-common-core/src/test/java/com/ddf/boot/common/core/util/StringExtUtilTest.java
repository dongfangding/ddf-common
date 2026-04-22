package com.ddf.boot.common.core.util;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * StringExtUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
public class StringExtUtilTest {

    @Test
    @DisplayName("测试 isEn 应判断是否全为英文字母")
    public void testIsEn() {
        Assertions.assertTrue(StringExtUtil.isEn("HelloWorld"));
        Assertions.assertTrue(StringExtUtil.isEn("hello"));
        Assertions.assertTrue(StringExtUtil.isEn("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        Assertions.assertFalse(StringExtUtil.isEn("Hello123"));
        Assertions.assertFalse(StringExtUtil.isEn("HelloWorld!"));
        Assertions.assertFalse(StringExtUtil.isEn("你好"));
        Assertions.assertFalse(StringExtUtil.isEn(""));
    }

    @Test
    @DisplayName("测试 randomLoginPassword 应返回合理长度密码")
    public void testRandomLoginPassword() {
        for (int i = 0; i < 20; i++) {
            String password = StringExtUtil.randomLoginPassword();
            Assertions.assertNotNull(password);
            Assertions.assertTrue(password.length() >= 6 && password.length() <= 17);
            Assertions.assertFalse("@#*".contains(String.valueOf(password.charAt(0))));
        }
    }

    @Test
    @DisplayName("测试 randomLoginPassword 指定范围时应按边界修正")
    public void testRandomLoginPasswordWithRange() {
        String minPassword = StringExtUtil.randomLoginPassword(4, 10);
        Assertions.assertTrue(minPassword.length() >= 7);

        String maxPassword = StringExtUtil.randomLoginPassword(6, 20);
        Assertions.assertTrue(maxPassword.length() <= 17);
    }

    @Test
    @DisplayName("测试 randomPayPassword 应生成 6 位纯数字")
    public void testRandomPayPassword() {
        String payPassword = StringExtUtil.randomPayPassword();
        Assertions.assertNotNull(payPassword);
        Assertions.assertEquals(6, payPassword.length());
        Assertions.assertTrue(Pattern.compile("^[0-9]+$").matcher(payPassword).matches());
    }

    @Test
    @DisplayName("测试 randomString 应至少满足指定长度")
    public void testRandomString() {
        String randomStr1 = StringExtUtil.randomString(10);
        String randomStr2 = StringExtUtil.randomString(32);

        Assertions.assertNotNull(randomStr1);
        Assertions.assertNotNull(randomStr2);
        Assertions.assertTrue(randomStr1.length() >= 10);
        Assertions.assertTrue(randomStr2.length() >= 32);
    }

    @Test
    @DisplayName("测试 exceptionToStringNoLimit 应包含异常信息")
    public void testExceptionToStringNoLimit() {
        try {
            throw new RuntimeException("测试异常");
        } catch (RuntimeException e) {
            String result = StringExtUtil.exceptionToStringNoLimit(e);
            Assertions.assertNotNull(result);
            Assertions.assertTrue(result.contains("RuntimeException"));
            Assertions.assertTrue(result.contains("测试异常"));
        }
    }

    @Test
    @DisplayName("测试 getShortNameBySplit 应按分隔符拼接首字符")
    public void testGetShortNameBySplit() {
        Assertions.assertEquals("c-d-b-c", StringExtUtil.getShortNameBySplit("com-ddf-boot-common", "-"));
        Assertions.assertEquals("H_W_T", StringExtUtil.getShortNameBySplit("Hello-World-Test", "-", "_"));
        Assertions.assertEquals("single", StringExtUtil.getShortNameBySplit("single", "-"));
    }

    @Test
    @DisplayName("测试 getFirstLowerCaseName 应转小写首字母")
    public void testGetFirstLowerCaseName() {
        Assertions.assertEquals("helloService", StringExtUtil.getFirstLowerCaseName("HelloService"));
        Assertions.assertEquals("userDao", StringExtUtil.getFirstLowerCaseName("UserDao"));
        Assertions.assertEquals("a", StringExtUtil.getFirstLowerCaseName("A"));
    }

    @Test
    @DisplayName("测试 isValidHttpScheme 应识别 HTTP 协议")
    public void testIsValidHttpScheme() {
        Assertions.assertTrue(StringExtUtil.isValidHttpScheme("http://example.com"));
        Assertions.assertTrue(StringExtUtil.isValidHttpScheme("https://example.com"));
        Assertions.assertFalse(StringExtUtil.isValidHttpScheme("ftp://example.com"));
        Assertions.assertFalse(StringExtUtil.isValidHttpScheme("example.com"));
        Assertions.assertFalse(StringExtUtil.isValidHttpScheme(" https://example.com "));
        Assertions.assertFalse(StringExtUtil.isValidHttpScheme(null));
        Assertions.assertFalse(StringExtUtil.isValidHttpScheme(""));
    }

    @Test
    @DisplayName("测试 parseBoolean 应兼容 1 和 true")
    public void testParseBoolean() {
        Assertions.assertTrue(StringExtUtil.parseBoolean("1"));
        Assertions.assertTrue(StringExtUtil.parseBoolean("true"));
        Assertions.assertTrue(StringExtUtil.parseBoolean("TRUE"));
        Assertions.assertFalse(StringExtUtil.parseBoolean("0"));
        Assertions.assertFalse(StringExtUtil.parseBoolean("false"));
        Assertions.assertFalse(StringExtUtil.parseBoolean(null));
    }
}
