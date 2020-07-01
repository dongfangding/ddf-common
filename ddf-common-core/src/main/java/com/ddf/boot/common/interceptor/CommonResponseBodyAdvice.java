package com.ddf.boot.common.interceptor;

import com.ddf.boot.common.response.ResponseData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;

/**
 * 允许在执行一个@ResponseBody 或一个ResponseEntity控制器方法之后但在使用一个主体写入正文之前自定义响应HttpMessageConverter。
 *
 * 如果不想要自己的返回值生效，可以在控制器类上使用@Controller代替@RestController或者{@link CommonResponseBodyAdviceProperties}
 *
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
 *
 * @author dongfang.ding
 * @date 2019/6/27 11:15
 */
@RestControllerAdvice(basePackages = {"com"}) // fixme 如何能够接受配置参数呢？如${basePackages}
@Order
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommonResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final CommonResponseBodyAdviceProperties commonResponseBodyAdviceProperties;

    private static final Class[] ANNOTATIONS = {
            RequestMapping.class,
            GetMapping.class,
            PostMapping.class,
            DeleteMapping.class,
            PutMapping.class
    };

    /**
     * @param returnType
     * @param converterType
     * @return
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        List<String> ignoreReturnType = commonResponseBodyAdviceProperties.getIgnoreReturnType();
        if (ignoreReturnType != null && ignoreReturnType.contains(returnType.getGenericParameterType().getTypeName())) {
            return false;
        }
        AnnotatedElement element = returnType.getAnnotatedElement();
        return Arrays.stream(ANNOTATIONS).anyMatch(annotation -> annotation.isAnnotation() && element.isAnnotationPresent(annotation));
    }

    @Override
    public ResponseData<Object> beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType
            , Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request
            , ServerHttpResponse response) {
        return ResponseData.success(body);
    }
}
