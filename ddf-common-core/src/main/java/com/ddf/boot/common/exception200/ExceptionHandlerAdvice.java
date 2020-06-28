package com.ddf.boot.common.exception200;

import cn.hutool.core.collection.CollectionUtil;
import com.ddf.boot.common.config.GlobalProperties;
import com.ddf.boot.common.helper.EnvironmentHelper;
import com.ddf.boot.common.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/06/28 10:20
 */
@RestControllerAdvice
@Slf4j
@ConditionalOnProperty(prefix = "customs", name = "global-properties.exception200", havingValue = "true")
public class ExceptionHandlerAdvice {

    private GlobalProperties globalProperties;

    private ExceptionHandlerMapping exceptionHandlerMapping;

    private EnvironmentHelper environmentHelper;

    @Autowired
    public void setGlobalProperties(GlobalProperties globalProperties) {
        this.globalProperties = globalProperties;
    }

    @Autowired(required = false)
    public void setExceptionHandlerMapping(ExceptionHandlerMapping exceptionHandlerMapping) {
        this.exceptionHandlerMapping = exceptionHandlerMapping;
    }

    @Autowired
    public void setEnvironmentHelper(EnvironmentHelper environmentHelper) {
        this.environmentHelper = environmentHelper;
    }


    /**
     * 处理异常类，某些异常类需要特殊处理，在具体根据当前异常去判断是否是期望的异常类型,
     * 这样可以只使用一个方法来处理，否则方法太多，看起来有点凌乱，也不太好做一些通用处理
     * @param exception
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseData<?> handlerException(Exception exception) {
        if (exceptionHandlerMapping != null) {
            ResponseData<?> responseData = exceptionHandlerMapping.handlerException(exception);
            if (responseData != null) {
                return responseData;
            }
        }

        // 是否将当前错误堆栈信息返回，默认返回，但提供某些环境下隐藏信息
        boolean ignoreErrorStack = false;
        List<String> ignoreErrorTraceProfile = globalProperties.getIgnoreErrorTraceProfile();
        if (CollectionUtil.isNotEmpty(ignoreErrorTraceProfile) && environmentHelper.checkIsExistOr(ignoreErrorTraceProfile)) {
            ignoreErrorStack = true;
        }

        if (exception instanceof BaseException) {
            BaseException baseException = (BaseException) exception;
            return ResponseData.failure(baseException.getCode(), baseException.getMessage(),
                    ignoreErrorStack ? "" : ExceptionUtils.getStackTrace(exception));
        } else if (exception instanceof IllegalArgumentException) {
            return ResponseData.failure(BaseErrorCallbackCode.BAD_REQUEST.getCode(), exception.getMessage(),
                    ignoreErrorStack ? "" : ExceptionUtils.getStackTrace(exception));
        }
        return ResponseData.failure(BaseErrorCallbackCode.SERVER_ERROR.getCode(), exception.getMessage(),
                ignoreErrorStack ? "" : ExceptionUtils.getStackTrace(exception));
    }

}
