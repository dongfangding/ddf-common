package com.ddf.boot.common.mvc.controllerwrapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ddf.boot.common.api.model.common.response.ResponseData;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * AbstractCommonResponseBodyAdvice 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class AbstractCommonResponseBodyAdviceTest {

    private final AbstractCommonResponseBodyAdvice advice = new AbstractCommonResponseBodyAdvice();

    @Test
    @DisplayName("映射方法应支持统一包装")
    void shouldSupportMappedMethod() throws Exception {
        Method method = DemoController.class.getDeclaredMethod("getUser");
        MethodParameter methodParameter = new MethodParameter(method, -1);

        assertTrue(advice.supports(methodParameter, StringHttpMessageConverter.class));
    }

    @Test
    @DisplayName("带 WrapperIgnore 的方法不应包装")
    void shouldIgnoreMethodAnnotatedWithWrapperIgnore() throws Exception {
        Method method = DemoController.class.getDeclaredMethod("ignore");
        MethodParameter methodParameter = new MethodParameter(method, -1);

        assertFalse(advice.supports(methodParameter, StringHttpMessageConverter.class));
    }

    @Test
    @DisplayName("非映射方法不应包装")
    void shouldNotSupportUnmappedMethod() throws Exception {
        Method method = DemoController.class.getDeclaredMethod("plainMethod");
        MethodParameter methodParameter = new MethodParameter(method, -1);

        assertFalse(advice.supports(methodParameter, StringHttpMessageConverter.class));
    }

    @Test
    @DisplayName("beforeBodyWrite 应包装为 ResponseData.success")
    void shouldWrapBodyIntoSuccessResponse() throws Exception {
        Method method = DemoController.class.getDeclaredMethod("getUser");
        MethodParameter methodParameter = new MethodParameter(method, -1);

        ResponseData<Object> responseData = advice.beforeBodyWrite(
            "payload",
            methodParameter,
            MediaType.APPLICATION_JSON,
            StringHttpMessageConverter.class,
            new ServletServerHttpRequest(new MockHttpServletRequest("GET", "/demo/user")),
            new ServletServerHttpResponse(new MockHttpServletResponse())
        );

        assertTrue(responseData.isSuccess());
        assertTrue("payload".equals(responseData.getData()));
    }

    @RequestMapping("/demo")
    static class DemoController {

        @GetMapping("/user")
        public String getUser() {
            return "ok";
        }

        @WrapperIgnore
        @GetMapping("/ignore")
        public String ignore() {
            return "ignore";
        }

        public String plainMethod() {
            return "plain";
        }
    }
}
