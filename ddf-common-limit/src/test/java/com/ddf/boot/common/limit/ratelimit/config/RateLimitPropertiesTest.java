package com.ddf.boot.common.limit.ratelimit.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ddf.boot.common.limit.ratelimit.keygenerator.GlobalRateLimitKeyGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * RateLimitProperties 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class RateLimitPropertiesTest {

    @Test
    @DisplayName("合法参数检查应通过")
    void shouldPassCheckWhenPropertiesAreValid() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setKeyGenerator(GlobalRateLimitKeyGenerator.BEAN_NAME);
        properties.setMax(10);
        properties.setRate(2);

        assertDoesNotThrow(properties::check);
    }

    @Test
    @DisplayName("keyGenerator 为空时应抛出异常")
    void shouldThrowWhenKeyGeneratorIsBlank() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setKeyGenerator(" ");
        properties.setMax(10);
        properties.setRate(2);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, properties::check);

        assertEquals("限流key组件生成器参数异常", exception.getMessage());
    }

    @Test
    @DisplayName("max 为空或负数时应抛出异常")
    void shouldThrowWhenMaxIsNullOrNegative() {
        RateLimitProperties nullMaxProperties = new RateLimitProperties();
        nullMaxProperties.setKeyGenerator(GlobalRateLimitKeyGenerator.BEAN_NAME);
        nullMaxProperties.setMax(null);
        nullMaxProperties.setRate(1);

        IllegalArgumentException nullException = assertThrows(IllegalArgumentException.class, nullMaxProperties::check);
        assertEquals("令牌桶最大数量参数异常", nullException.getMessage());

        RateLimitProperties negativeMaxProperties = new RateLimitProperties();
        negativeMaxProperties.setKeyGenerator(GlobalRateLimitKeyGenerator.BEAN_NAME);
        negativeMaxProperties.setMax(-1);
        negativeMaxProperties.setRate(1);

        IllegalArgumentException negativeException = assertThrows(IllegalArgumentException.class,
                negativeMaxProperties::check);
        assertEquals("令牌桶最大数量参数异常", negativeException.getMessage());
    }

    @Test
    @DisplayName("rate 为空或负数时应抛出异常")
    void shouldThrowWhenRateIsNullOrNegative() {
        RateLimitProperties nullRateProperties = new RateLimitProperties();
        nullRateProperties.setKeyGenerator(GlobalRateLimitKeyGenerator.BEAN_NAME);
        nullRateProperties.setMax(10);
        nullRateProperties.setRate(null);

        IllegalArgumentException nullException = assertThrows(IllegalArgumentException.class,
                nullRateProperties::check);
        assertEquals("令牌恢复速率参数异常", nullException.getMessage());

        RateLimitProperties negativeRateProperties = new RateLimitProperties();
        negativeRateProperties.setKeyGenerator(GlobalRateLimitKeyGenerator.BEAN_NAME);
        negativeRateProperties.setMax(10);
        negativeRateProperties.setRate(-1);

        IllegalArgumentException negativeException = assertThrows(IllegalArgumentException.class,
                negativeRateProperties::check);
        assertEquals("令牌恢复速率参数异常", negativeException.getMessage());
    }
}
