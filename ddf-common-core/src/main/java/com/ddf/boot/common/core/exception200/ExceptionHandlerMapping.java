package com.ddf.boot.common.core.exception200;

import com.ddf.boot.common.core.response.ResponseData;

/**
 * <p>将捕获的异常暴露出去，允许实现方实现接口根据这个异常自定义返回数据</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/06/28 10:33
 */
public interface ExceptionHandlerMapping {

    /**
     * 捕捉到的异常给实现方自定义实现返回数据
     *
     *
     * @param exception
     * @see ExceptionHandlerAdvice#handlerException(java.lang.Exception)
     *
     * @return 如果当前异常不是自己要处理的类型，请返回{@code null}
     */
    ResponseData<?> handlerException(Exception exception);
}
