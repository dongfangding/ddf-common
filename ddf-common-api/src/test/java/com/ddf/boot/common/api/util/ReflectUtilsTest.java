package com.ddf.boot.common.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ReflectUtils 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class ReflectUtilsTest {

    @Test
    @DisplayName("setFiledValue 应能写入私有字段")
    void shouldSetPrivateFieldValue() throws Exception {
        DemoChild target = new DemoChild();

        ReflectUtils.setFiledValue(target, "name", "codex", true);

        assertEquals("codex", target.getName());
    }

    @Test
    @DisplayName("getFields 应返回本类与父类字段")
    void shouldReturnFieldsFromClassHierarchy() {
        Field[] fields = ReflectUtils.getFields(DemoChild.class);

        assertTrue(fields.length >= 2);
        assertTrue(java.util.Arrays.stream(fields).anyMatch(field -> "name".equals(field.getName())));
        assertTrue(java.util.Arrays.stream(fields).anyMatch(field -> "parentValue".equals(field.getName())));
    }

    @Test
    @DisplayName("getMethod 应返回可访问方法，不存在时返回 null")
    void shouldReturnAccessibleMethod() {
        Method method = ReflectUtils.getMethod(DemoChild.class, "getName");
        Method missingMethod = ReflectUtils.getMethod(DemoChild.class, "notExists");

        assertNotNull(method);
        assertEquals("getName", method.getName());
        assertNull(missingMethod);
    }

    public static class DemoParent {
        private String parentValue = "parent";
    }

    public static class DemoChild extends DemoParent {
        private String name;

        public String getName() {
            return name;
        }
    }
}
