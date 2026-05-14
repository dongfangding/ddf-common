package com.ddf.boot.common.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * VerifyCodeUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class VerifyCodeUtilTest {

    @Test
    @DisplayName("generateVerifyCode 应按长度和字符源生成验证码")
    void shouldGenerateVerifyCodeWithExpectedLengthAndCharset() {
        String code = VerifyCodeUtil.generateVerifyCode(6);
        Set<Character> allowed = VerifyCodeUtil.VERIFY_CODES.chars().mapToObj(ch -> (char) ch).collect(
                Collectors.toSet());

        assertEquals(6, code.length());
        assertTrue(code.chars().allMatch(ch -> allowed.contains((char) ch)));
    }

    @Test
    @DisplayName("自定义字符源为空时应回退默认字符集")
    void shouldFallbackToDefaultSourcesWhenCustomSourceIsBlank() {
        String code = VerifyCodeUtil.generateVerifyCode(4, "");
        Set<Character> allowed = VerifyCodeUtil.VERIFY_CODES.chars().mapToObj(ch -> (char) ch).collect(
                Collectors.toSet());

        assertEquals(4, code.length());
        assertTrue(code.chars().allMatch(ch -> allowed.contains((char) ch)));
    }

    @Test
    @DisplayName("outputImage 应输出非空 jpg 图片流")
    void shouldOutputImageBytes() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        VerifyCodeUtil.outputImage(120, 40, outputStream, "ABCD");

        byte[] bytes = outputStream.toByteArray();
        assertTrue(bytes.length > 0);
        assertEquals((byte) 0xFF, bytes[0]);
    }
}
