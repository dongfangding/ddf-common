package com.ddf.boot.common.mvc.exception200;

import com.ddf.boot.common.api.model.common.response.ResponseData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 通用包异常拦截$
 *
 * @author dongfang.ding
 * @since 2020/11/22 0022 22:11
 */
@RestControllerAdvice(basePackages = "com.ddf.boot.common")
public class CommonExceptionAdvice extends AbstractExceptionHandler {

    /**
     * 异常处理
     *
     * @param exception 异常对象
     * @param httpServletRequest httpservlet请求参数
     * @param response 响应对象
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    @Override
    public ResponseData<?> handlerException(Exception exception, HttpServletRequest httpServletRequest,
            HttpServletResponse response) {
        return super.handlerException(exception, httpServletRequest, response);
    }
}
