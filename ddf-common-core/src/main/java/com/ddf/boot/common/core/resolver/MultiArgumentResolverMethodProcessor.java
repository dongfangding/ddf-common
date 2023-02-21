package com.ddf.boot.common.core.resolver;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodParameter;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

/**
 * 自定义参数解析器用以支持同一个参数支持application/json和application/x-www-form-urlencoded解析
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/08/31 19:00
 * @see MultiArgumentResolver
 */
@Slf4j
public class MultiArgumentResolverMethodProcessor implements HandlerMethodArgumentResolver, ApplicationContextAware,
        SmartInitializingSingleton {

    private ApplicationContext applicationContext;

    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    public MultiArgumentResolverMethodProcessor() {
    }


    private static final String CONTENT_TYPE_JSON = "application/json";

    private static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";


    /**
     * 支持的content_type
     */
    private static final ImmutableList<String> SUPPORT_CONTENT_TYPE_LIST = ImmutableList.of(CONTENT_TYPE_JSON, CONTENT_TYPE_FORM_URLENCODED);

    /**
     * 参考这个写法， 同一个类型的参数解析后缓存对应的参数解析器，不过这里的key改为了Content-Type
     * @see HandlerMethodArgumentResolverComposite#argumentResolverCache
     */
    private final Map<String, HandlerMethodArgumentResolver> argumentResolverCache =
            new ConcurrentHashMap<>(8);


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameter().isAnnotationPresent(MultiArgumentResolver.class);
    }

    /**
     * 解析参数
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String contentType = webRequest.getHeader("Content-Type");
        isSupport(contentType);
        List<HandlerMethodArgumentResolver> argumentResolvers = requestMappingHandlerAdapter.getArgumentResolvers();
        HandlerMethodArgumentResolver handlerMethodArgumentResolver = argumentResolverCache.get(contentType);
        if (handlerMethodArgumentResolver != null) {
            return handlerMethodArgumentResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        }
        for (HandlerMethodArgumentResolver argumentResolver : argumentResolvers) {
            if (isJson(contentType) && argumentResolver instanceof RequestResponseBodyMethodProcessor) {
                argumentResolverCache.put(contentType, argumentResolver);
                return argumentResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
            } else if (isFormUrlEncoded(contentType) && argumentResolver instanceof ServletModelAttributeMethodProcessor) {
                argumentResolverCache.put(contentType, argumentResolver);
                return argumentResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
            }
        }
        return null;
    }

    private boolean isJson(String contentType) {
        return contentType.contains(CONTENT_TYPE_JSON);
    }

    private boolean isFormUrlEncoded(String contentType) {
        return contentType.contains(CONTENT_TYPE_FORM_URLENCODED);
    }

    /**
     * 判断当前参数解析器是否支持解析当前的Content-Type
     * @param contentType
     * @return
     * @throws HttpMediaTypeNotSupportedException
     */
    private boolean isSupport(String contentType) throws HttpMediaTypeNotSupportedException {
        if (contentType == null) {
            throw new HttpMediaTypeNotSupportedException("contentType不能为空");
        }
        boolean isMatch = false;
        for (String item : SUPPORT_CONTENT_TYPE_LIST) {
            if (contentType.contains(item)) {
                isMatch = true;
                break;
            }
        }
        if (!isMatch) {
            throw new HttpMediaTypeNotSupportedException("支持Content-Type" + SUPPORT_CONTENT_TYPE_LIST.toString());
        }
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        this.requestMappingHandlerAdapter = applicationContext.getBean(RequestMappingHandlerAdapter.class);
    }
}
