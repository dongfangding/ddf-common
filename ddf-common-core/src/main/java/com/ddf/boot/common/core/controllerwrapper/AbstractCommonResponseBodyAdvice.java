package com.ddf.boot.common.core.controllerwrapper;

import com.ddf.boot.common.api.model.common.response.response.ResponseData;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 允许在执行一个@ResponseBody 或一个ResponseEntity控制器方法之后但在使用一个主体写入正文之前自定义响应HttpMessageConverter。
 * 如果不想要自己的返回值生效，可以在控制器类上使用@Controller代替@RestController或者{@link CommonResponseBodyAdviceProperties}
 *
 * 这里提供逻辑，但没有将该类加入到容器中，就是为了让其他服务直接集成该类，然后再添加自己的@RestControllerAdvice(basePackages = {""})
 *
 * @author dongfang.ding
 * @date 2019/6/27 11:15
 */
public class AbstractCommonResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final Class[] ANNOTATIONS = {
            RequestMapping.class, GetMapping.class, PostMapping.class, DeleteMapping.class, PutMapping.class
    };

    /**
     * @param returnType
     * @param converterType
     * @return
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (returnType.getMethod() == null || returnType.getMethod().isAnnotationPresent(WrapperIgnore.class)) {
            return false;
        }
        AnnotatedElement element = returnType.getAnnotatedElement();
        return Arrays.stream(ANNOTATIONS).anyMatch(
                annotation -> annotation.isAnnotation() && element.isAnnotationPresent(annotation));
    }

    @Override
    public ResponseData<Object> beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response) {
        return ResponseData.success(body);
    }
}
