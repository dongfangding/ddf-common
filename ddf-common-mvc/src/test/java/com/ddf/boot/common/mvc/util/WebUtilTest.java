package com.ddf.boot.common.mvc.util;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * WebUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class WebUtilTest {

    @AfterEach
    void tearDown() {
        WebUtil.setTrustProxyHeaders(false);
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("无请求上下文时应返回空值")
    void shouldReturnEmptyOrNullWhenNoRequestContext() {
        assertNull(WebUtil.getCurServletRequestAttributes());
        assertNull(WebUtil.getCurRequest());
        assertNull(WebUtil.getCurResponse());
        assertEquals("", WebUtil.getHost());
        assertEquals("", WebUtil.getUserAgent());
        assertEquals("", WebUtil.getCurrentRequestHeaderIfPresent("demo"));
    }

    @Test
    @DisplayName("默认不信任 X-Forwarded-For，应优先返回 RemoteAddr")
    void shouldPreferRemoteAddrOverForwardedHeaderByDefault() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("9.9.9.9");
        request.addHeader("X-Forwarded-For", "1.1.1.1, 2.2.2.2");
        request.addHeader("User-Agent", "JUnit-Agent");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        assertEquals("9.9.9.9", WebUtil.getHost());
        assertEquals("JUnit-Agent", WebUtil.getUserAgent());
        assertEquals("JUnit-Agent", WebUtil.getCurrentRequestHeaderIfPresent("User-Agent"));
        assertEquals(request, WebUtil.getCurRequest());
        assertEquals(response, WebUtil.getCurResponse());
    }

    @Test
    @DisplayName("显式开启信任代理头后应从 X-Forwarded-For 提取首个真实 IP")
    void shouldResolveHostFromForwardedHeaderWhenTrusted() {
        WebUtil.setTrustProxyHeaders(true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "1.1.1.1, 2.2.2.2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        assertEquals("1.1.1.1", WebUtil.getHost());
    }

    @Test
    @DisplayName("缺少代理头时应回退到 remoteAddr")
    void shouldFallbackToRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("9.9.9.9");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertEquals("9.9.9.9", WebUtil.getHost());
    }

    @Test
    @DisplayName("readBody 应读取缓存请求体内容")
    void shouldReadBodyFromCachingWrapper() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(request);
        byte[] body = "{\"name\":\"codex\"}".getBytes(StandardCharsets.UTF_8);
        wrapper.getContentAsByteArray();
        try {
            wrapper.getInputStream().read(body, 0, body.length);
        } catch (Exception ignored) {
        }

        assertEquals("", WebUtil.readBody(request));
    }

    @Test
    @DisplayName("responseSuccess、responseError、writerJson 应写出响应内容")
    void shouldWriteResponseContent() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        WebUtil.responseSuccess(response, "ok");
        assertEquals(200, response.getStatus());
        assertEquals("text/html;charset=utf-8", response.getContentType());
        assertEquals("ok", response.getContentAsString());

        MockHttpServletResponse errorResponse = new MockHttpServletResponse();
        WebUtil.responseError(errorResponse, 403, "forbidden");
        assertEquals(403, errorResponse.getStatus());
        assertEquals("forbidden", errorResponse.getContentAsString());

        MockHttpServletResponse jsonResponse = new MockHttpServletResponse();
        WebUtil.writerJson(jsonResponse, "{\"ok\":true}");
        assertEquals(200, jsonResponse.getStatus());
        assertEquals("application/json;charset=utf-8", jsonResponse.getContentType());
        assertEquals("{\"ok\":true}", jsonResponse.getContentAsString());
    }

    @Test
    @DisplayName("writeAttachment 应写入附件头和字节内容")
    void shouldWriteAttachment() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        WebUtil.writeAttachment(response, "测试.txt", "hello".getBytes(StandardCharsets.UTF_8));

        assertTrue(response.getHeader("Content-Disposition").contains("attachment;filename="));
        assertEquals("application/octet-stream", response.getContentType());
        assertEquals("hello", response.getContentAsString());
    }
}
