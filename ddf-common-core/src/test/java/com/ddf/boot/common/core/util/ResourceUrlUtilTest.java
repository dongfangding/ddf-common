package com.ddf.boot.common.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ResourceUrlUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class ResourceUrlUtilTest {

    @Test
    @DisplayName("wrapAbsolutePath 应补齐相对路径并保持绝对路径不变")
    void shouldWrapAbsolutePath() {
        assertEquals(
            "https://cdn.example.com/avatar.png",
            ResourceUrlUtil.wrapAbsolutePath("https://cdn.example.com", "avatar.png")
        );
        assertEquals(
            "http://static.example.com/avatar.png",
            ResourceUrlUtil.wrapAbsolutePath("https://cdn.example.com", "http://static.example.com/avatar.png")
        );
        assertNull(ResourceUrlUtil.wrapAbsolutePath(null, "avatar.png"));
    }

    @Test
    @DisplayName("wrapRelativePath 应从绝对路径中截取前缀")
    void shouldWrapRelativePath() {
        assertEquals(
            "/avatar.png",
            ResourceUrlUtil.wrapRelativePath("https://cdn.example.com", "https://cdn.example.com/avatar.png")
        );
        assertEquals(
            "avatar.png",
            ResourceUrlUtil.wrapRelativePath("https://cdn.example.com", "avatar.png")
        );
        assertNull(ResourceUrlUtil.wrapRelativePath("https://cdn.example.com", null));
    }
}
