package com.ddf.boot.common.core.exception200;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.net.NetUtil;
import com.ddf.boot.common.core.config.GlobalProperties;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.boot.common.core.response.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * <p>description</p >
 *
 *
 * FIXME 这种方式，无法决定系统使用方的basePackages， 所以这里暂时用了com
 *
 * 也可以将该类定义为抽象普通类，不再交给spring管理， 然后应用使用方自己定义拦截规则， 继承这个类， 使用父类的逻辑，这里只提供逻辑
 * 这么做，至少也能保证如果在微服务项目中的话，可以统一管理多个模块的异常处理机制
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/06/28 10:20
 */
@Slf4j
@Component
public abstract class AbstractExceptionHandler {

    @Autowired
    private GlobalProperties globalProperties;
    @Autowired(required = false)
    private ExceptionHandlerMapping exceptionHandlerMapping;
    @Autowired
    private EnvironmentHelper environmentHelper;
    @Autowired(required = false)
    private MessageSource messageSource;

    /**
     * 处理异常类，某些异常类需要特殊处理，在具体根据当前异常去判断是否是期望的异常类型,
     * 这样可以只使用一个方法来处理，否则方法太多，看起来有点凌乱，也不太好做一些通用处理
     * @param exception
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseData<?> handlerException(Exception exception, HttpServletRequest httpServletRequest, HttpServletResponse response) {
        log.error("异常信息: ", exception);
        if (exceptionHandlerMapping != null) {
            ResponseData<?> responseData = exceptionHandlerMapping.handlerException(exception);
            if (responseData != null) {
                return responseData;
            }
        }

        // 是否将当前错误堆栈信息返回，默认返回，但提供某些环境下隐藏信息
        boolean ignoreErrorStack = false;
        String extraServerMessage = String.format("-[%s:%s]", environmentHelper.getApplicationName(), NetUtil.getLocalhostStr());
        List<String> ignoreErrorTraceProfile = globalProperties.getIgnoreErrorTraceProfile();
        if (CollectionUtil.isNotEmpty(ignoreErrorTraceProfile) && environmentHelper.checkIsExistOr(ignoreErrorTraceProfile)) {
            ignoreErrorStack = true;
            extraServerMessage = "";
        }

        String exceptionCode;
        String message;
        if (exception instanceof BaseException) {
            BaseException baseException = (BaseException) exception;
            exceptionCode = baseException.getCode();
            // 解析异常类消息代码，并根据当前Local格式化资源文件
            Locale locale = httpServletRequest.getLocale();
            // 没有定义资源文件的使用直接使用异常消息，定义了这里会根据异常状态码走i18n资源文件
            message = messageSource.getMessage(baseException.getCode(), baseException.getParams(),
                    exception.getMessage(), locale);
        } else if (exception instanceof IllegalArgumentException ) {
            exceptionCode = BaseErrorCallbackCode.BAD_REQUEST.getCode();
            message = exception.getMessage();
        } else if (exception instanceof MethodArgumentNotValidException) {
            exceptionCode = BaseErrorCallbackCode.BAD_REQUEST.getCode();
            MethodArgumentNotValidException exception1  = (MethodArgumentNotValidException) exception;
            message = exception1.getBindingResult().getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(";"));
        } else {
            exceptionCode = BaseErrorCallbackCode.SERVER_ERROR.getCode();
            message = exception.getMessage();
        }
        // 附加信息
        message += extraServerMessage;
        if (globalProperties.isExceptionCodeToResponseStatus()) {
            String numberRegex = "\\d+";
            // 可能会出现超过int最大值的问题，暂时不管
            if (exceptionCode.matches(numberRegex)) {
                response.setStatus(Integer.parseInt(exceptionCode));
            } else {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }
        return ResponseData.failure(exceptionCode, message, ignoreErrorStack ? "" : ExceptionUtils.getStackTrace(exception));
    }
}
