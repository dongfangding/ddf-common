package com.ddf.boot.common.core.exception200;

import com.ddf.boot.common.api.model.response.ResponseData;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 通用包异常拦截$
 *
 * @author dongfang.ding
 * @date 2020/11/22 0022 22:11
 */
@Component
@RestControllerAdvice(basePackages = "com.ddf.boot.common")
public class CommonExceptionAdvice extends AbstractExceptionHandler {

    /**
     * 异常处理
     *
     * @param exception
     * @param httpServletRequest
     * @param response
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
