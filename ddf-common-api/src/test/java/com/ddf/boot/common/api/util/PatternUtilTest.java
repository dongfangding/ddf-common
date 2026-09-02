package com.ddf.boot.common.api.util;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PatternUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class PatternUtilTest {

    @Test
    @DisplayName("应能提取 html 中全部图片链接并去重")
    void shouldFindImageSourceUrlsFromHtml() {
        String html = "<div>" + "<img src=\"https://img.example.com/a.png\" alt=\"a\"/>"
                + "<IMG src='https://img.example.com/b.jpg' />" + "<img src=\"https://img.example.com/a.png\"/>"
                + "</div>";

        Set<String> urls = PatternUtil.findImgSrcUrl(html);

        assertEquals(2, urls.size());
        assertTrue(urls.contains("https://img.example.com/a.png"));
        assertTrue(urls.contains("'https://img.example.com/b.jpg'"));
    }

    @Test
    @DisplayName("应按正则统计子串出现次数")
    void shouldCountMatchedChildString() {
        int count = PatternUtil.findChildStrCount("abc-123-ABC-456", "abc");

        assertEquals(2, count);
    }

    @Test
    @DisplayName("应从 jdbc url 中提取数据库名")
    void shouldExtractDatabaseNameFromJdbcUrl() {
        String databaseName = PatternUtil.extractDatabaseName(
                "jdbc:mysql://127.0.0.1:3306/demo_db?useUnicode=true&characterEncoding=UTF-8");

        assertEquals("demo_db", databaseName);
    }

    @Test
    @DisplayName("非法 jdbc url 应抛出异常")
    void shouldThrowWhenJdbcUrlIsInvalid() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PatternUtil.extractDatabaseName("jdbc:postgresql://127.0.0.1:5432/demo"));

        assertEquals("Invalid database URL format.", exception.getMessage());
    }
}
