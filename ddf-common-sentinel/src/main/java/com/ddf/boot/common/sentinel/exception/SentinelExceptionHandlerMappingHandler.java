package com.ddf.boot.common.sentinel.exception;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.ddf.boot.common.core.response.ResponseData;
import com.ddf.boot.common.core.util.StringExtUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * sentinel异常扩展处理器, 这里将这里单独抽出来是因为异常接管只允许一个实现， 这里如果默认实现的话， 应用层又想使用这个逻辑， 又想加自己的，
 * 就很麻烦，所以这里将这里的逻辑抽成方法， 应用层可直接调用
 *
 * @author dongfang.ding
 * @date 2020/12/8 0008 22:50
 */
public class SentinelExceptionHandlerMappingHandler {
    /**
     * 捕捉到的异常给实现方自定义实现返回数据
     *
     * @param exception
     * @return 如果当前异常不是自己要处理的类型，请返回{@code null}
     * @see com.ddf.boot.common.core.exception200.AbstractExceptionHandler#handlerException(Exception, HttpServletRequest, HttpServletResponse)
     */
    public ResponseData<?> handlerException(Exception exception) {
        if (exception instanceof BlockException) {
            return ResponseData.failure("429", "当前访问次数过多，请稍后再试^_^", StringExtUtil.exceptionToStringNoLimit(exception));
        }
        // 如果不是流控异常， 还是让上层处理异常， 使用null来标识自己不处理
        return null;
    }
}
