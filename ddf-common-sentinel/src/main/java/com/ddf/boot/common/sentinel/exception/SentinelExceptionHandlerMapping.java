package com.ddf.boot.common.sentinel.exception;

import com.ddf.boot.common.api.model.common.response.response.ResponseData;
import com.ddf.boot.common.core.exception200.ExceptionHandlerMapping;

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
     */
    @Override
    public ResponseData<?> handlerException(Exception exception) {
        return handler.handlerException(exception);
    }
}
