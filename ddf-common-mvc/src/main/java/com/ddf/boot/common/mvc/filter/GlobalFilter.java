package com.ddf.boot.common.mvc.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * <p>全局预定义的拦截器， 在这个拦截器里做一些通用处理，然后再暴露接口，方便外部织入有顺序的filter</p >
 *
 * @author snowball
 * @version 1.0
 * @date 2022/01/14 17:37
 */
@Slf4j
public class GlobalFilter implements HandlerInterceptor {

}
