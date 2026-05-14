package com.ddf.boot.common.mvc.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

/**
 * MultiArgumentResolverMethodProcessor 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class MultiArgumentResolverMethodProcessorTest {

    @Test
    @DisplayName("仅支持带 MultiArgumentResolver 注解的参数")
    void shouldSupportOnlyAnnotatedParameter() throws Exception {
        MultiArgumentResolverMethodProcessor processor = new MultiArgumentResolverMethodProcessor();
        Method supportedMethod = DemoController.class.getDeclaredMethod("supported", DemoBody.class);
        Method unsupportedMethod = DemoController.class.getDeclaredMethod("unsupported", DemoBody.class);

        assertTrue(processor.supportsParameter(new MethodParameter(supportedMethod, 0)));
        assertEquals(false, processor.supportsParameter(new MethodParameter(unsupportedMethod, 0)));
    }

    @Test
    @DisplayName("json 请求应选择 RequestResponseBodyMethodProcessor 并缓存")
    void shouldResolveJsonRequestAndCacheResolver() throws Exception {
        MultiArgumentResolverMethodProcessor processor = new MultiArgumentResolverMethodProcessor();
        RequestMappingHandlerAdapter adapter = mock(RequestMappingHandlerAdapter.class);
        RequestResponseBodyMethodProcessor jsonResolver = mock(RequestResponseBodyMethodProcessor.class);
        Method method = DemoController.class.getDeclaredMethod("supported", DemoBody.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        NativeWebRequest webRequest = new ServletWebRequest(buildRequest("application/json;charset=UTF-8"));

        when(adapter.getArgumentResolvers()).thenReturn(List.of(jsonResolver));
        doReturn("json-result").when(jsonResolver).resolveArgument(parameter, null, webRequest, null);
        setField(processor, "requestMappingHandlerAdapter", adapter);

        Object firstResult = processor.resolveArgument(parameter, null, webRequest, null);
        Object secondResult = processor.resolveArgument(parameter, null, webRequest, null);

        assertEquals("json-result", firstResult);
        assertEquals("json-result", secondResult);
        verify(jsonResolver, times(2)).resolveArgument(parameter, null, webRequest, null);
        verify(adapter, times(2)).getArgumentResolvers();
    }

    @Test
    @DisplayName("form 请求应选择 ServletModelAttributeMethodProcessor")
    void shouldResolveFormRequest() throws Exception {
        MultiArgumentResolverMethodProcessor processor = new MultiArgumentResolverMethodProcessor();
        RequestMappingHandlerAdapter adapter = mock(RequestMappingHandlerAdapter.class);
        ServletModelAttributeMethodProcessor formResolver = mock(ServletModelAttributeMethodProcessor.class);
        Method method = DemoController.class.getDeclaredMethod("supported", DemoBody.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        NativeWebRequest webRequest = new ServletWebRequest(buildRequest("application/x-www-form-urlencoded"));

        when(adapter.getArgumentResolvers()).thenReturn(List.of(formResolver));
        doReturn("form-result").when(formResolver).resolveArgument(parameter, null, webRequest, null);
        setField(processor, "requestMappingHandlerAdapter", adapter);

        Object result = processor.resolveArgument(parameter, null, webRequest, null);

        assertEquals("form-result", result);
    }

    @Test
    @DisplayName("不支持或缺失 content-type 时应抛出异常")
    void shouldThrowWhenContentTypeIsUnsupported() throws Exception {
        MultiArgumentResolverMethodProcessor processor = new MultiArgumentResolverMethodProcessor();
        Method method = DemoController.class.getDeclaredMethod("supported", DemoBody.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        HttpMediaTypeNotSupportedException nullException = assertThrows(HttpMediaTypeNotSupportedException.class,
                () -> processor.resolveArgument(parameter, new ModelAndViewContainer(),
                        new ServletWebRequest(new MockHttpServletRequest()), null));
        assertTrue(nullException.getMessage().contains("contentType"));

        MockHttpServletRequest xmlRequest = buildRequest("application/xml");
        HttpMediaTypeNotSupportedException xmlException = assertThrows(HttpMediaTypeNotSupportedException.class,
                () -> processor.resolveArgument(parameter, null, new ServletWebRequest(xmlRequest), null));
        assertTrue(xmlException.getMessage().contains("Content-Type"));
    }

    @Test
    @DisplayName("afterSingletonsInstantiated 应从容器获取 RequestMappingHandlerAdapter")
    void shouldInitializeAdapterFromApplicationContext() throws Exception {
        MultiArgumentResolverMethodProcessor processor = new MultiArgumentResolverMethodProcessor();
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        RequestMappingHandlerAdapter adapter = mock(RequestMappingHandlerAdapter.class);

        when(applicationContext.getBean(RequestMappingHandlerAdapter.class)).thenReturn(adapter);

        processor.setApplicationContext(applicationContext);
        processor.afterSingletonsInstantiated();

        assertEquals(adapter, getField(processor, "requestMappingHandlerAdapter"));
    }

    private static MockHttpServletRequest buildRequest(String contentType) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Content-Type", contentType);
        return request;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object getField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    static class DemoController {

        public void supported(@MultiArgumentResolver DemoBody body) {
        }

        public void unsupported(DemoBody body) {
        }
    }


    static class DemoBody {
    }
}
