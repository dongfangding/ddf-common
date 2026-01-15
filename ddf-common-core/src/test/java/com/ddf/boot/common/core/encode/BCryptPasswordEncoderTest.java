package com.ddf.boot.common.core.encode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BCryptPasswordEncoder 测试类
 *
 * @author X_Agent
 * @date 2025/01/15
 */
public class BCryptPasswordEncoderTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("测试 encode - 基本密码加密")
    public void testEncode() {
        String rawPassword = "password123";
        String encodedPassword = encoder.encode(rawPassword);

        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword, "编码后的密码不应与原始密码相同");
        assertTrue(encodedPassword.startsWith("$2a$"), "BCrypt编码应该以$2a$开头");
        assertTrue(encodedPassword.length() > 50, "BCrypt编码长度应该大于50");
    }

    @Test
    @DisplayName("测试 matches - 正确密码匹配")
    public void testMatchesCorrectPassword() {
        String rawPassword = "MySecurePassword123!";
        String encodedPassword = encoder.encode(rawPassword);

        assertTrue(encoder.matches(rawPassword, encodedPassword), "正确密码应该匹配");
    }

    @Test
    @DisplayName("测试 matches - 错误密码不匹配")
    public void testMatchesWrongPassword() {
        String rawPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String encodedPassword = encoder.encode(rawPassword);

        assertFalse(encoder.matches(wrongPassword, encodedPassword), "错误密码不应该匹配");
    }

    @Test
    @DisplayName("测试 matches - 多次编码仍可匹配")
    public void testMatchesAfterMultipleEncodes() {
        String rawPassword = "testPassword";

        // 同一密码多次encode结果不同（因为salt不同）
        String encoded1 = encoder.encode(rawPassword);
        String encoded2 = encoder.encode(rawPassword);

        assertNotEquals(encoded1, encoded2, "同一密码多次编码结果应不同");

        // 但都应该能匹配原始密码
        assertTrue(encoder.matches(rawPassword, encoded1), "第一次编码应能匹配");
        assertTrue(encoder.matches(rawPassword, encoded2), "第二次编码应能匹配");
    }

    @Test
    @DisplayName("测试 matches - 空密码处理")
    public void testMatchesEmptyPassword() {
        assertFalse(encoder.matches("", "anyEncoded"), "空密码不应匹配任何编码");
    }

    @Test
    @DisplayName("测试 matches - null编码密码")
    public void testMatchesNullEncodedPassword() {
        assertFalse(encoder.matches("password", null), "null编码不应匹配");
    }

    @Test
    @DisplayName("测试 matches - 空编码密码")
    public void testMatchesEmptyEncodedPassword() {
        assertFalse(encoder.matches("password", ""), "空编码不应匹配");
    }

    @Test
    @DisplayName("测试 matches - 无效BCrypt格式")
    public void testMatchesInvalidFormat() {
        assertFalse(encoder.matches("password", "notAValidBcryptHash"), "无效格式不应匹配");
        assertFalse(encoder.matches("password", "md5hash123abc"), "非BCrypt格式不应匹配");
        assertFalse(encoder.matches("password", "$2x$invalidhash"), "无效版本不应匹配");
    }

    @Test
    @DisplayName("测试 encode - null密码")
    public void testEncodeNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> encoder.encode(null), "null密码应抛出异常");
    }

    @Test
    @DisplayName("测试 upgradeEncoding - 需要升级")
    public void testUpgradeEncodingNeedsUpgrade() {
        // 使用较低强度编码
        BCryptPasswordEncoder weakEncoder = new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2A, 4);
        String weakEncoded = weakEncoder.encode("password");

        // 默认编码器强度为10，应该提示升级
        assertTrue(encoder.upgradeEncoding(weakEncoded), "低强度编码应提示升级");
    }

    @Test
    @DisplayName("测试 upgradeEncoding - 不需要升级")
    public void testUpgradeEncodingNoUpgrade() {
        String encoded = encoder.encode("password");
        assertFalse(encoder.upgradeEncoding(encoded), "当前强度编码不应提示升级");
    }

    @Test
    @DisplayName("测试 upgradeEncoding - 空编码")
    public void testUpgradeEncodingEmpty() {
        assertFalse(encoder.upgradeEncoding(""), "空编码不应提示升级");
    }

    @Test
    @DisplayName("测试 upgradeEncoding - null编码")
    public void testUpgradeEncodingNull() {
        assertFalse(encoder.upgradeEncoding(null), "null编码不应提示升级");
    }

    @Test
    @DisplayName("测试 upgradeEncoding - 无效格式")
    public void testUpgradeEncodingInvalidFormat() {
        assertThrows(IllegalArgumentException.class,
                () -> encoder.upgradeEncoding("invalidHash"),
                "无效格式应抛出异常");
    }

    @Test
    @DisplayName("测试不同强度编码器")
    public void testDifferentStrengthEncoder() {
        BCryptPasswordEncoder weakEncoder = new BCryptPasswordEncoder(4);
        BCryptPasswordEncoder defaultEncoder = new BCryptPasswordEncoder();
        BCryptPasswordEncoder strongEncoder = new BCryptPasswordEncoder(15);

        String weakPassword = weakEncoder.encode("password");
        String strongPassword = strongEncoder.encode("password");

        assertTrue(encoder.matches("password", weakPassword));
        assertTrue(encoder.matches("password", strongPassword));

        // 默认编码器（强度10）应提示弱编码升级
        assertTrue(defaultEncoder.upgradeEncoding(weakPassword), "默认编码器应提示弱编码升级");
        assertFalse(defaultEncoder.upgradeEncoding(strongPassword), "默认编码器不应提示强编码升级");

        // 弱编码器自己的编码不需要升级（强度相同）
        assertFalse(weakEncoder.upgradeEncoding(weakPassword), "弱编码器自己的编码不应升级");
    }

    @Test
    @DisplayName("测试不同版本编码器")
    public void testDifferentVersionEncoder() {
        BCryptPasswordEncoder encoder2a = new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2A);
        BCryptPasswordEncoder encoder2b = new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2B);

        String password2a = encoder2a.encode("password");
        String password2b = encoder2b.encode("password");

        assertTrue(encoder2a.matches("password", password2a));
        assertTrue(encoder2b.matches("password", password2b));

        assertTrue(password2a.startsWith("$2a$"));
        assertTrue(password2b.startsWith("$2b$"));
    }

    @Test
    @DisplayName("测试使用SecureRandom的编码器")
    public void testEncoderWithSecureRandom() {
        SecureRandom secureRandom = new SecureRandom();
        BCryptPasswordEncoder encoderWithRandom = new BCryptPasswordEncoder(
                BCryptPasswordEncoder.BCryptVersion.$2A, 10, secureRandom);

        String encoded1 = encoderWithRandom.encode("password");
        String encoded2 = encoderWithRandom.encode("password");

        assertNotEquals(encoded1, encoded2, "使用SecureRandom时编码结果应不同");
        assertTrue(encoderWithRandom.matches("password", encoded1));
        assertTrue(encoderWithRandom.matches("password", encoded2));
    }

    @Test
    @DisplayName("测试密码强度足够")
    public void testPasswordStrengthSufficient() {
        String[] testPasswords = {
                "simple",
                "Password123",
                "VeryLongPasswordThatIsMuchMoreThan16Characters!@#",
                "中文字符密码"
        };

        for (String password : testPasswords) {
            String encoded = encoder.encode(password);
            assertNotNull(encoded);
            assertTrue(encoder.matches(password, encoded), "密码 " + password + " 应该能匹配");
        }
    }

    @Test
    @DisplayName("测试特殊字符密码")
    public void testSpecialCharacterPassword() {
        String specialPassword = "p@$$w0rd!#$%^&*()_+-=[]{}|;':\",./<>?";
        String encoded = encoder.encode(specialPassword);

        assertNotNull(encoded);
        assertTrue(encoder.matches(specialPassword, encoded), "特殊字符密码应该能匹配");
    }

    @Test
    @DisplayName("测试长时间密码")
    public void testLongPassword() {
        String longPassword = "A".repeat(1000);
        String encoded = encoder.encode(longPassword);

        assertNotNull(encoded);
        assertTrue(encoder.matches(longPassword, encoded), "长密码应该能匹配");
    }
}
