package com.ddf.boot.common.mvc.filter;


import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.ContentCachingRequestWrapper;



/**
 * 在Spring MVC中，它提供了类ContentCachingRequestWrapper，它会对原始的HttpServletRequest对象进行包装。 当我们调用request body时，
 * ContentCachingRequestWrapper会把request body的内容进行缓存，这样我们就可以在后续的使用重复读取request body。
 *
 * @author yiming
 * @date 2024/3/21 14:38
 **/
public class CachingRequestBodyFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest currentRequest = (HttpServletRequest) request;
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(currentRequest);
        chain.doFilter(wrappedRequest, response);
    }
}
