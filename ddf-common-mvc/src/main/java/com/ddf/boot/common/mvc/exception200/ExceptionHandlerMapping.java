package com.ddf.boot.common.mvc.exception200;

import com.ddf.boot.common.api.exception.BaseCallbackCode;
import com.ddf.boot.common.api.model.common.response.ResponseData;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>将捕获的异常暴露出去，允许实现方实现接口根据这个异常自定义返回数据</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/06/28 10:33
 */
public interface ExceptionHandlerMapping {

    /**
     * 捕捉到的异常给实现方自定义实现返回数据
     *
     * @param exception
     * @return 如果当前异常不是自己要处理的类型，请返回{@code null}
     * @see AbstractExceptionHandler#handlerException(Exception, HttpServletRequest, HttpServletResponse)
     */
    ResponseData<?> takeOverException(Exception exception);


    /**
     * 解析异常，通用类中由于包引用的限制，无法判定所有的异常，未判定的异常只能统一服务端异常，这个提供一个判定，可以匹配异常，返回自己的异常状态码
     *
     * @param exception
     * @return
     */
    BaseCallbackCode resolveException(Exception exception);



}
