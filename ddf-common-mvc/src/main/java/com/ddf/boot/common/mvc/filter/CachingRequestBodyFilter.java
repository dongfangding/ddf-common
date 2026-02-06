package com.ddf.boot.common.mvc.filter;


import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.ContentCachingRequestWrapper;



/**
 * 在Spring MVC中，它提供了类ContentCachingRequestWrapper，它会对原始的HttpServletRequest对象进行包装。 当我们调用request body时，
 * ContentCachingRequestWrapper会把request body的内容进行缓存，这样我们就可以在后续的使用重复读取request body。
 *
 * @author yiming
 * @since 2024/3/21 14:38
 **/
public class CachingRequestBodyFilter extends GenericFilterBean {

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response,
            jakarta.servlet.FilterChain chain) throws IOException, jakarta.servlet.ServletException {
        HttpServletRequest currentRequest = (HttpServletRequest) request;
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(currentRequest);
        chain.doFilter(wrappedRequest, response);
    }
}
