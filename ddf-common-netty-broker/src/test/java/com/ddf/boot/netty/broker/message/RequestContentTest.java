package com.ddf.boot.netty.broker.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * RequestContent 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class RequestContentTest {

    @Test
    @DisplayName("应构造服务端主动请求和心跳请求")
    void shouldBuildRequestAndHeartbeat() {
        RequestContent<String> request = RequestContent.request("LOGIN", "payload");
        RequestContent<Void> heart = RequestContent.heart();

        assertNotNull(request.getRequestId());
        assertEquals(RequestContent.Type.REQUEST, request.getType());
        assertEquals(RequestContent.SEND_MODE_SERVER, request.getSendMode());
        assertEquals("LOGIN", request.getCmd());
        assertEquals("payload", request.getBody());

        assertEquals(RequestContent.Cmd.PING.name(), heart.getCmd());
        assertEquals(RequestContent.Type.REQUEST, heart.getType());
    }

    @Test
    @DisplayName("应基于请求构造成功与接收响应")
    void shouldBuildResponsesFromRequest() {
        RequestContent<String> request = RequestContent.request("SYNC", "body");
        request.setClientChannel("mqtt");

        RequestContent<String> accept = RequestContent.responseAccept(request);
        RequestContent<Integer> success = RequestContent.responseSuccess(request, 123);

        assertEquals(request.getRequestId(), accept.getRequestId());
        assertEquals(RequestContent.Type.RESPONSE, accept.getType());
        assertEquals(ResponseCodeEnum.CODE_RECEIVED.getCode(), accept.getCode());
        assertEquals("mqtt", accept.getClientChannel());

        assertEquals(ResponseCodeEnum.CODE_COMPLETE.getCode(), success.getCode());
        assertEquals(123, success.getBody());
        assertEquals(RequestContent.SEND_MODE_SERVER, success.getSendMode());
    }

    @Test
    @DisplayName("应追加并解析扩展头")
    void shouldAddAndParseExtraHeaders() {
        RequestContent<String> request = RequestContent.request("SYNC", "body");

        request.addExtra("traceId", "trace-1").addExtra("lang", "zh-CN");

        assertEquals("trace-1", request.getExtraMap().get("traceId"));
        assertEquals("zh-CN", request.getExtraMap().get("lang"));
        assertTrue(request.getExtra().contains("traceId: trace-1"));
        assertTrue(request.getExtra().contains("lang: zh-CN"));
    }

    @Test
    @DisplayName("响应码枚举应支持按 code 查找")
    void shouldResolveResponseCodeByCode() {
        assertEquals(ResponseCodeEnum.CODE_COMPLETE, ResponseCodeEnum.CODE_ERROR.getByCode(200));
        assertEquals(ResponseCodeEnum.UNAUTHORIZED, ResponseCodeEnum.CODE_ERROR.getByCode(401));
        assertNull(ResponseCodeEnum.CODE_ERROR.getByCode(999));
    }
}
