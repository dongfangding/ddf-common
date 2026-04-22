package com.ddf.boot.common.mvc.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ddf.boot.common.api.model.common.dto.QueryParam;
import com.ddf.boot.common.core.util.ContextKey;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * QueryParamArgumentResolver 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class QueryParamArgumentResolverTest {

    private final QueryParamArgumentResolver resolver = new QueryParamArgumentResolver();

    @Test
    @DisplayName("仅支持 List<QueryParam> 参数")
    void shouldSupportOnlyListOfQueryParam() throws Exception {
        Method supportedMethod = DemoController.class.getDeclaredMethod("supported", List.class);
        Method unsupportedMethod = DemoController.class.getDeclaredMethod("unsupported", String.class);
        Method otherGenericMethod = DemoController.class.getDeclaredMethod("otherGeneric", List.class);

        assertTrue(resolver.supportsParameter(new MethodParameter(supportedMethod, 0)));
        assertFalse(resolver.supportsParameter(new MethodParameter(unsupportedMethod, 0)));
        assertFalse(resolver.supportsParameter(new MethodParameter(otherGenericMethod, 0)));
    }

    @Test
    @DisplayName("缺少 queryParams 参数时应返回空集合")
    void shouldReturnEmptyListWhenQueryParamsIsBlank() throws Exception {
        Method method = DemoController.class.getDeclaredMethod("supported", List.class);
        NativeWebRequest webRequest = new ServletWebRequest(new MockHttpServletRequest());

        List<QueryParam> result = resolver.resolveArgument(
            new MethodParameter(method, 0),
            null,
            webRequest,
            null
        );

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("应解析 queryParams 并填充默认 relative 与 op")
    void shouldResolveQueryParamsAndApplyDefaults() throws Exception {
        Method method = DemoController.class.getDeclaredMethod("supported", List.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(
            ContextKey.queryParams.name(),
            "[{\"key\":\"name\",\"value\":\"codex\"},{\"key\":\"age\",\"op\":\"GT\",\"value\":18,\"relative\":\"OR\"}]"
        );
        NativeWebRequest webRequest = new ServletWebRequest(request);

        List<QueryParam> result = resolver.resolveArgument(
            new MethodParameter(method, 0),
            null,
            webRequest,
            null
        );

        assertEquals(2, result.size());
        assertEquals("name", result.get(0).getKey());
        assertEquals(QueryParam.Op.EQ, result.get(0).getOp());
        assertEquals(QueryParam.Relative.AND, result.get(0).getRelative());
        assertEquals("age", result.get(1).getKey());
        assertEquals(QueryParam.Op.GT, result.get(1).getOp());
        assertEquals(QueryParam.Relative.OR, result.get(1).getRelative());
    }

    static class DemoController {

        public void supported(List<QueryParam> queryParams) {
        }

        public void unsupported(String queryParams) {
        }

        public void otherGeneric(List<String> values) {
        }
    }
}
