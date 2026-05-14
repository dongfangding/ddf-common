package com.ddf.boot.common.core.util;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ObjectUtil 测试类
 *
 * @author X_Agent
 * @since 2025/01/15
 */
public class ObjectUtilTest {

    @Test
    @DisplayName("测试 checkAndGet - 对象为null时返回null")
    public void testCheckAndGetWithNullObject() {
        String result = ObjectUtil.checkAndGet(null, () -> "default");
        Assertions.assertNull(result);
    }

    @Test
    @DisplayName("测试 checkAndGet - 对象不为null时执行supplier")
    public void testCheckAndGetWithNonNullObject() {
        String testString = "test";
        String result = ObjectUtil.checkAndGet(testString, () -> testString.toUpperCase());
        Assertions.assertEquals("TEST", result);
    }

    @Test
    @DisplayName("测试 checkAndGet - 使用Optional场景")
    public void testCheckAndGetWithOptional() {
        String value = "hello";
        String result = ObjectUtil.checkAndGet(value, () -> Optional.of(value).map(String::toUpperCase).orElse(null));
        Assertions.assertEquals("HELLO", result);
    }

    @Test
    @DisplayName("测试 getOrDefault - 对象为null时返回默认值")
    public void testGetOrDefaultWithNullObject() {
        String result = ObjectUtil.getOrDefault(null, "default", () -> null);
        Assertions.assertEquals("default", result);
    }

    @Test
    @DisplayName("测试 getOrDefault - 对象不为null但supplier结果为null")
    public void testGetOrDefaultWithNullSupplierResult() {
        String testString = "test";
        String result = ObjectUtil.getOrDefault(testString, "default", () -> null);
        Assertions.assertEquals("default", result);
    }

    @Test
    @DisplayName("测试 getOrDefault - 对象和supplier结果都不为null")
    public void testGetOrDefaultWithNonNull() {
        String testString = "test";
        String result = ObjectUtil.getOrDefault(testString, "default", () -> testString.toUpperCase());
        Assertions.assertEquals("TEST", result);
    }

    @Test
    @DisplayName("测试 getOrDefault - 数字类型默认值")
    public void testGetOrDefaultWithNumber() {
        Integer result = ObjectUtil.getOrDefault(null, 100, () -> null);
        Assertions.assertEquals(Integer.valueOf(100), result);

        Integer result2 = ObjectUtil.getOrDefault(5, 100, () -> 5 * 2);
        Assertions.assertEquals(Integer.valueOf(10), result2);
    }

    @Test
    @DisplayName("测试 getOrDefault - 集合类型默认值")
    public void testGetOrDefaultWithCollection() {
        java.util.List<String> result = ObjectUtil.getOrDefault(null, java.util.Collections.emptyList(),
                java.util.List::of);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }
}
