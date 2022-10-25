package com.ddf.boot.common.sentinel.exception;

import com.ddf.boot.common.api.model.response.ResponseData;
import com.ddf.boot.common.core.exception200.AbstractExceptionHandler;
import com.ddf.boot.common.core.exception200.ExceptionHandlerMapping;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * sentinel流控异常处理类$
 *
 * @author dongfang.ding
 * @date 2020/12/8 0008 22:57
 */
public class SentinelExceptionHandlerMapping implements ExceptionHandlerMapping {

    private final SentinelExceptionHandlerMappingHandler handler;

    public SentinelExceptionHandlerMapping(SentinelExceptionHandlerMappingHandler handler) {
        this.handler = handler;
    }

    /**
     * 捕捉到的异常给实现方自定义实现返回数据
     *
     * @param exception
     * @return 如果当前异常不是自己要处理的类型，请返回{@code null}
     * @see AbstractExceptionHandler#handlerException(Exception, HttpServletRequest, HttpServletResponse)
     */
    @Override
    public ResponseData<?> handlerException(Exception exception) {
        return handler.handlerException(exception);
    }
}
