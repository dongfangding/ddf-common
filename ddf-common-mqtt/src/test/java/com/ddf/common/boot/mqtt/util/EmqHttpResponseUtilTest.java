package com.ddf.common.boot.mqtt.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * EmqHttpResponseUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class EmqHttpResponseUtilTest {

    @Test
    @DisplayName("success 应写入成功响应")
    void shouldWriteSuccessResponse() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter, true);
        when(response.getWriter()).thenReturn(printWriter);

        EmqHttpResponseUtil.success(response, "ok");

        verify(response).setStatus(200);
        verify(response).setCharacterEncoding("UTF-8");
        assertEquals("ok", stringWriter.toString());
    }

    @Test
    @DisplayName("error 应写入失败响应")
    void shouldWriteErrorResponse() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter, true);
        when(response.getWriter()).thenReturn(printWriter);

        EmqHttpResponseUtil.error(response, "denied");

        verify(response).setStatus(401);
        verify(response).setCharacterEncoding("UTF-8");
        assertEquals("denied", stringWriter.toString());
    }

    @Test
    @DisplayName("写响应异常时不应继续抛出")
    void shouldSwallowIoExceptionWhenWritingResponse() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        doThrow(new IOException("mock io exception")).when(response).getWriter();

        EmqHttpResponseUtil.success(response, "ok");
        EmqHttpResponseUtil.error(response, "denied");

        verify(response, times(2)).setCharacterEncoding("UTF-8");
    }
}
