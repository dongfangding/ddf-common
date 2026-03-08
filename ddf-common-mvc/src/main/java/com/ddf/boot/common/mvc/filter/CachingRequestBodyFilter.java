package com.ddf.boot.common.mvc.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;



/**
 * 在Spring MVC中，它提供了类ContentCachingRequestWrapper，它会对原始的HttpServletRequest对象进行包装。 当我们调用request body时，
 * ContentCachingRequestWrapper会把request body的内容进行缓存，这样我们就可以在后续的使用重复读取request body。
 *
 * @author yiming
 * @since 2024/3/21 14:38
 **/
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CachingRequestBodyFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        HttpServletRequest currentRequest = (HttpServletRequest) request;

        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(currentRequest);
        }
        filterChain.doFilter(request, response);
    }
}
