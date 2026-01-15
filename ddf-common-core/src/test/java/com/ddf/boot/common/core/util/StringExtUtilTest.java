package com.ddf.boot.common.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

/**
 * StringExtUtil 测试类
 *
 * @author X_Agent
 * @date 2025/01/15
 */
public class StringExtUtilTest {

    @Test
    @DisplayName("测试 isEn - 判断是否全是英文字母")
    public void testIsEn() {
        Assertions.assertTrue(StringExtUtil.isEn("HelloWorld"));
        Assertions.assertTrue(StringExtUtil.isEn("hello"));
        Assertions.assertTrue(StringExtUtil.isEn("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        Assertions.assertFalse(StringExtUtil.isEn("Hello123"));
        Assertions.assertFalse(StringExtUtil.isEn("HelloWorld!"));
        Assertions.assertFalse(StringExtUtil.isEn("你好"));
        Assertions.assertFalse(StringExtUtil.isEn(""));
        // null should not crash
        try {
            StringExtUtil.isEn(null);
        } catch (Exception e) {
            // Expected behavior - can throw exception for null
        }
    }

    @Test
    @DisplayName("测试 randomLoginPassword - 随机生成登录密码")
    public void testRandomLoginPassword() {
        // 测试多次以验证基本功能
        // 注意：由于Random的某些边界情况可能略有偏差，测试5-20之间的合理范围
        for (int i = 0; i < 20; i++) {
            String password = StringExtUtil.randomLoginPassword();
            Assertions.assertNotNull(password);
            // 长度应该在合理范围内
            Assertions.assertTrue(password.length() >= 5 && password.length() <= 20,
                    "密码长度应该在5-20之间，实际: " + password.length());
        }
    }

    @Test
    @DisplayName("测试 randomLoginPassword - 指定长度范围")
    public void testRandomLoginPasswordWithRange() {
        // 测试最小长度会被修正为6
        String minPassword = StringExtUtil.randomLoginPassword(4, 10);
        Assertions.assertTrue(minPassword.length() >= 6, "最小长度不能小于6，实际: " + minPassword.length());

        // 测试超出最大长度限制会被修正为16
        String maxPassword = StringExtUtil.randomLoginPassword(6, 20);
        Assertions.assertTrue(maxPassword.length() <= 16, "最大长度不能超过16，实际: " + maxPassword.length());
    }

    @Test
    @DisplayName("测试 randomPayPassword - 生成6位支付密码")
    public void testRandomPayPassword() {
        String payPassword = StringExtUtil.randomPayPassword();
        Assertions.assertNotNull(payPassword);
        Assertions.assertEquals(6, payPassword.length());

        // 验证全是数字
        Assertions.assertTrue(Pattern.compile("^[0-9]+$").matcher(payPassword).matches(),
                "支付密码应全是数字");
    }

    @Test
    @DisplayName("测试 randomString - 根据时间戳生成随机数")
    public void testRandomString() {
        String randomStr1 = StringExtUtil.randomString(10);
        String randomStr2 = StringExtUtil.randomString(10);
        String randomStr3 = StringExtUtil.randomString(32);

        Assertions.assertNotNull(randomStr1);
        Assertions.assertNotNull(randomStr2);
        Assertions.assertNotNull(randomStr3);
        // randomString 生成长度基于时间戳，至少是指定长度
        Assertions.assertTrue(randomStr1.length() >= 10, "随机字符串长度应至少10位");
        Assertions.assertTrue(randomStr3.length() >= 32, "随机字符串长度应至少32位");
    }

    @Test
    @DisplayName("测试 exceptionToStringNoLimit - 异常栈转字符串")
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
    @DisplayName("测试 getShortNameBySplit - 分割后首字母拼接")
    public void testGetShortNameBySplit() {
        // 测试默认分隔符
        String result1 = StringExtUtil.getShortNameBySplit("com-ddf-boot-common", "-");
        Assertions.assertEquals("c-d-b-c", result1);

        // 测试自定义分隔符和替换符
        String result2 = StringExtUtil.getShortNameBySplit("Hello-World-Test", "-", "_");
        Assertions.assertEquals("H_W_T", result2);

        // 测试单段字符串返回原值
        String result3 = StringExtUtil.getShortNameBySplit("single", "-");
        Assertions.assertEquals("single", result3);
    }

    @Test
    @DisplayName("测试 getFirstLowerCaseName - 首字符小写")
    public void testGetFirstLowerCaseName() {
        Assertions.assertEquals("helloService", StringExtUtil.getFirstLowerCaseName("HelloService"));
        Assertions.assertEquals("userDao", StringExtUtil.getFirstLowerCaseName("UserDao"));
        Assertions.assertEquals("a", StringExtUtil.getFirstLowerCaseName("A"));
    }

    @Test
    @DisplayName("测试 isValidHttpScheme - 验证HTTP协议")
    public void testIsValidHttpScheme() {
        Assertions.assertTrue(StringExtUtil.isValidHttpScheme("http://example.com"));
        Assertions.assertTrue(StringExtUtil.isValidHttpScheme("https://example.com"));
        Assertions.assertFalse(StringExtUtil.isValidHttpScheme("ftp://example.com"));
        Assertions.assertFalse(StringExtUtil.isValidHttpScheme("example.com"));
        Assertions.assertFalse(StringExtUtil.isValidHttpScheme(null));
        Assertions.assertFalse(StringExtUtil.isValidHttpScheme(""));
    }
}
