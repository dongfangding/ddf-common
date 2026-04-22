package com.ddf.boot.common.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ddf.boot.common.api.exception.ServerErrorException;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * JsonUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class JsonUtilTest {

    @Test
    @DisplayName("toJson 传入空对象时应返回空字符串")
    void shouldReturnEmptyStringWhenObjectIsNull() {
        assertEquals("", JsonUtil.toJson(null));
    }

    @Test
    @DisplayName("toJson 传入字符串时应直接返回原值")
    void shouldReturnOriginalStringWhenInputIsString() {
        assertEquals("plain-text", JsonUtil.toJson("plain-text"));
    }

    @Test
    @DisplayName("toBean 传入空白 JSON 时应返回 null")
    void shouldReturnNullWhenJsonIsBlank() {
        assertNull(JsonUtil.toBean("", DemoPayload.class));
        assertNull(JsonUtil.toBeanChecked("   ", DemoPayload.class));
        assertNull(JsonUtil.toBeanChecked(null, DemoPayload.class));
    }

    @Test
    @DisplayName("应支持 JavaTime 类型对象往返序列化")
    void shouldRoundTripJavaTimeFields() {
        DemoPayload payload = new DemoPayload(
                "demo",
                LocalDate.of(2026, 4, 20),
                LocalDateTime.of(2026, 4, 20, 11, 22, 33),
                null
        );

        String json = JsonUtil.toJson(payload);
        DemoPayload parsed = JsonUtil.toBean(json, DemoPayload.class);

        assertNotNull(parsed);
        assertEquals(payload.getName(), parsed.getName());
        assertEquals(payload.getDate(), parsed.getDate());
        assertEquals(payload.getDateTime(), parsed.getDateTime());
    }

    @Test
    @DisplayName("按策略序列化时应忽略 null 字段")
    void shouldSerializeWithConfiguredIncludeStrategy() {
        DemoPayload payload = new DemoPayload("demo", null, null, null);

        String json = JsonUtil.toJson(payload, JsonInclude.Include.NON_NULL);

        assertTrue(json.contains("\"name\":\"demo\""));
        assertTrue(!json.contains("date"));
    }

    @Test
    @DisplayName("toList 传入空白 JSON 时应返回空集合")
    void shouldReturnEmptyListWhenJsonForListIsBlank() {
        List<DemoPayload> result = JsonUtil.toList("", DemoPayload.class);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("toList 应支持集合反序列化")
    void shouldDeserializeListPayload() {
        String json = "[{\"name\":\"a\"},{\"name\":\"b\"}]";

        List<DemoPayload> result = JsonUtil.toList(json, DemoPayload.class);

        assertEquals(2, result.size());
        assertEquals("a", result.get(0).getName());
        assertEquals("b", result.get(1).getName());
    }

    @Test
    @DisplayName("非法 JSON 反序列化时应抛出统一服务异常")
    void shouldThrowServerErrorExceptionForInvalidJson() {
        assertThrows(ServerErrorException.class, () -> JsonUtil.toBean("{invalid", DemoPayload.class));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class DemoPayload {
        private String name;
        private LocalDate date;
        private LocalDateTime dateTime;
        private String ignored;
    }
}
