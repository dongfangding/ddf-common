package com.ddf.boot.common.mvc.exception200;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.ddf.boot.common.api.consts.AlarmLog;
import com.ddf.boot.common.api.exception.AlarmException;
import com.ddf.boot.common.api.exception.BaseCallbackCode;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BaseException;
import com.ddf.boot.common.api.model.common.request.RequestHeaderEnum;
import com.ddf.boot.common.api.model.common.response.ResponseData;
import com.ddf.boot.common.core.config.GlobalProperties;
import com.ddf.boot.common.core.event.GlobalExceptionEvent;
import com.ddf.boot.common.core.event.GlobalExceptionEventPayload;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.boot.common.mvc.util.MessageSourceUtil;
import com.ddf.boot.common.mvc.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartException;

/**
 * <p>全局异常处理器，捕获并统一处理所有未捕获的异常。</p>
 * <p>
 * 应用方可以继承此类覆盖部分方法来自定义行为，或者实现 {@link ExceptionHandlerMapping}
 * 接口提供扩展点，无需修改核心逻辑。
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2020/06/28 10:20
 */
@Slf4j
public abstract class AbstractExceptionHandler {

    /**
     * 数字正则，用于判断异常码是否为 HTTP 状态码。
     */
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    /**
     * 默认语言环境。
     */
    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    /**
     * 默认版本号。
     */
    private static final String DEFAULT_VERSION = "0";

    /**
     * 默认布尔值。
     */
    private static final String DEFAULT_BOOLEAN = "false";

    /**
     * 异常日志中 body 的最大打印长度，超过则截断，避免完整打印敏感请求体。
     */
    private static final int MAX_BODY_LOG_LENGTH = 200;

    /**
     * 缓存本地主机地址，避免每次都进行 DNS 查询。
     */
    private static final String LOCAL_HOST_ADDRESS = initLocalHostAddress();

    @Autowired
    private GlobalProperties globalProperties;
    @Autowired
    private ObjectProvider<ExceptionHandlerMapping> exceptionHandlerMappingProvider;
    @Autowired
    private EnvironmentHelper environmentHelper;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private static String initLocalHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    /**
     * 处理异常类，某些异常类需要特殊处理，在具体根据当前异常去判断是否是期望的异常类型，
     * 这样可以只使用一个方法来处理，否则方法太多，看起来有点凌乱，也不太好做一些通用处理。
     *
     * @param exception 异常对象
     * @param httpServletRequest 参数
     * @param response 响应对象
     * @return 统一响应数据
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseData<?> handlerException(Exception exception, HttpServletRequest httpServletRequest,
            HttpServletResponse response) {
        String body = WebUtil.readBody(httpServletRequest);
        final String uri = httpServletRequest.getRequestURI();
        final String queryString = httpServletRequest.getQueryString();
        // 处理客户端传递的约定好的请求头
        final Map<String, String> clientHeaderMap = RequestHeaderEnum.resolveClientHeaders(httpServletRequest);
        final boolean shouldTriggerExceptionEvent = logException(exception, uri, queryString, clientHeaderMap, body);
        if (exception instanceof AlarmException) {
            AlarmLog.error("全局异常捕获到告警异常， 请求{}，异常堆栈: ", uri, exception);
        }

        final GlobalExceptionEventPayload payload = buildPayload(exception, uri, httpServletRequest, clientHeaderMap,
                body);

        // 允许扩展实现类接管异常处理，可以在业务层面实现一些异常情况下的额外处理，但记得如果不接管异常处理，最后要返回null
        final ExceptionHandlerMapping exceptionHandlerMapping = exceptionHandlerMappingProvider.getIfAvailable();
        if (exceptionHandlerMapping != null) {
            // 仅仅支持通知异常，提供一个回调的机制
            exceptionHandlerMapping.notifyException(httpServletRequest, exception);
            // 这里可以接管异常返回值，如果为null，继续走本类的逻辑，如果不为空，则走实现里返回的
            ResponseData<?> responseData = exceptionHandlerMapping.takeOverException(exception);
            if (responseData != null) {
                payload.setErrorMessage(responseData.getMessage());
                // 基于事件的话，可以多订阅多实现
                if (shouldTriggerExceptionEvent) {
                    applicationEventPublisher.publishEvent(new GlobalExceptionEvent(this, payload));
                }
                return responseData;
            }
        }

        final Locale locale = resolveLocale(httpServletRequest);
        final ExceptionResolveResult resolveResult = resolveExceptionMessage(exception, exceptionHandlerMapping);

        String exceptionCode = resolveResult.exceptionCode;
        String formatDefaultMessage = resolveResult.formatDefaultMessage;
        if (StringUtils.isBlank(exceptionCode)) {
            exceptionCode = BaseErrorCallbackCode.SERVER_ERROR.getCode();
            formatDefaultMessage = BaseErrorCallbackCode.SERVER_ERROR.getBizMessage();
        }

        final String finalMessage = MessageSourceUtil.getMessage(
                StringUtils.defaultIfBlank(resolveResult.formatCode, exceptionCode), resolveResult.formatParams,
                formatDefaultMessage, locale, StringUtils.isNotBlank(exceptionCode));

        // 根据异常资源文件格式化消息，找不到的话，使用默认异常本身的消息, 如果exceptionCode不为空，则国际化翻译文本可以缓存
        applyResponseStatus(response, exceptionCode);

        payload.setErrorCode(exceptionCode);
        payload.setErrorMessage(ExceptionUtil.stacktraceToString(exception));

        // 基于事件的话，可以多订阅多实现
        if (shouldTriggerExceptionEvent) {
            applicationEventPublisher.publishEvent(new GlobalExceptionEvent(this, payload));
        }
        return ResponseData.failure(exceptionCode, finalMessage, resolveResult.subMessage, resolveResult.extra,
                resolveResult.formatParams);
    }

    /**
     * 记录异常日志。
     */
    private boolean logException(Exception exception, String uri, String queryString,
            Map<String, String> clientHeaderMap, String body) {
        final List<String> ignoreLogExceptionClassName = globalProperties.getIgnoreLogExceptionClassName();
        final String exceptionClassName = exception.getClass().getName();

        if (CollUtil.isEmpty(ignoreLogExceptionClassName) || !ignoreLogExceptionClassName.contains(
                exceptionClassName)) {
            log.error(
                    "全局异常捕获到请求异常， url = {}, 请求参数: queryString = {}, body = {}, clientHeaders = {}, 异常堆栈: ",
                    uri, queryString, maskBody(body), maskHeaders(clientHeaderMap), exception);
            return true;
        }
        // 业务异常， 打印info日志，可以追溯查看，也不会污染error文件
        log.info("全局异常捕获到请求异常， url = {}, 请求参数: params = {}, body = {}, , clientHeaders = {}, 异常堆栈: ",
                uri, queryString, maskBody(body), maskHeaders(clientHeaderMap), exception);
        return false;
    }

    /**
     * 对请求体脱敏，避免把密码/token 等敏感信息完整打印进日志；超过长度只打印截断片段。
     */
    private String maskBody(String body) {
        if (StringUtils.isBlank(body)) {
            return "";
        }
        if (body.length() <= MAX_BODY_LOG_LENGTH) {
            return body;
        }
        return body.substring(0, MAX_BODY_LOG_LENGTH) + "...(截断, 原长度=" + body.length() + ")";
    }

    /**
     * 对请求头脱敏，只保留请求头名称，值统一掩码，避免 Authorization/token 等凭证泄露。
     */
    private Map<String, String> maskHeaders(Map<String, String> headers) {
        if (CollUtil.isEmpty(headers)) {
            return Collections.emptyMap();
        }
        Map<String, String> masked = new LinkedHashMap<>(headers.size());
        headers.forEach((k, v) -> masked.put(k, maskValue(v)));
        return masked;
    }

    /**
     * 对单个值掩码：长度过短直接打星，否则保留首尾各两位。
     */
    private String maskValue(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        if (value.length() <= 4) {
            return "***";
        }
        return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
    }

    /**
     * 构建异常事件负载对象。
     */
    private GlobalExceptionEventPayload buildPayload(Exception exception, String uri, HttpServletRequest request,
            Map<String, String> clientHeaderMap, String body) {
        final GlobalExceptionEventPayload payload = new GlobalExceptionEventPayload();
        payload.setUrl(uri);
        payload.setParameterMap(request.getParameterMap());
        payload.setBody(body);
        payload.setHost(LOCAL_HOST_ADDRESS);
        payload.setApplicationName(environmentHelper.getApplicationName());
        payload.setProfile(environmentHelper.getProfileStr());
        payload.setTimestamps(System.currentTimeMillis());
        payload.setImei(clientHeaderMap.get(RequestHeaderEnum.IMEI.getName()));
        payload.setOs(clientHeaderMap.get(RequestHeaderEnum.OS.getName()));
        payload.setUid(clientHeaderMap.get(RequestHeaderEnum.USER_ID_FROM_GATEWAY.getName()));
        payload.setIsGatewayDispatch(Boolean.parseBoolean(
                clientHeaderMap.getOrDefault(RequestHeaderEnum.IS_GATEWAY_DISPATCH.getName(), DEFAULT_BOOLEAN)));
        payload.setVersionCode(Integer.parseInt(
                clientHeaderMap.getOrDefault(RequestHeaderEnum.VERSION_CODE.getName(), DEFAULT_VERSION)));
        payload.setClientHeaderMap(clientHeaderMap);
        return payload;
    }

    /**
     * 解析请求语言环境。
     */
    private Locale resolveLocale(HttpServletRequest request) {
        final String appLanguage = request.getHeader(RequestHeaderEnum.LANGUAGE.getName());
        if (StringUtils.isNotBlank(appLanguage)) {
            try {
                //                return Locale.forLanguageTag(appLanguage);
                return new Locale(appLanguage);
            } catch (Exception e) {
                log.warn("解析App语言失败，header app_language={}", appLanguage);
            }
        }
        return DEFAULT_LOCALE;
    }

    /**
     * 设置 HTTP 响应状态码。
     */
    private void applyResponseStatus(HttpServletResponse response, String exceptionCode) {
        if (globalProperties.isExceptionCodeToResponseStatus()) {
            // 可能会出现超过int最大值的问题，暂时不管
            if (NUMBER_PATTERN.matcher(exceptionCode).matches()) {
                response.setStatus(Integer.parseInt(exceptionCode));
            } else {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }
    }


    /**
     * 解析业务异常消息。
     *
     * @param exception 异常对象
     * @param exceptionHandlerMapping 可选的异常处理器映射
     * @return 解析结果
     */
    public static ExceptionResolveResult resolveExceptionMessage(Exception exception,
            ExceptionHandlerMapping exceptionHandlerMapping) {
        try {
            // 原始异常描述
            String exceptionCode = "";
            String subMessage = exception.getMessage();
            Object extra = null;
            Object[] formatParams = new Object[] {};
            String formatCode = "";
            String formatDefaultMessage = "";

            if (exception instanceof BaseException baseException) {
                // 默认的异常状态码
                return resolveBaseException(baseException);
            }
            if (exception instanceof IllegalArgumentException) {
                return fromErrorCode(BaseErrorCallbackCode.BAD_REQUEST);
            }
            if (exception instanceof MultipartException) {
                return fromErrorCode(BaseErrorCallbackCode.UPLOAD_FILE_ERROR);
            }
            if (exception instanceof BindException bindException) {
                return resolveBindException(bindException);
            }
            if (exception instanceof org.springframework.dao.DuplicateKeyException
                    || exception instanceof SQLIntegrityConstraintViolationException) {
                return fromErrorCode(BaseErrorCallbackCode.DUPLICATE_KEY);
            }
            if (exceptionHandlerMapping != null) {
                final BaseCallbackCode baseCallbackCode = exceptionHandlerMapping.resolveOtherException(exception);
                if (Objects.nonNull(baseCallbackCode)) {
                    return fromErrorCode(baseCallbackCode);
                }
            }
            return new ExceptionResolveResult(exceptionCode, formatCode, formatDefaultMessage, formatParams, subMessage,
                    extra);
        } catch (Exception e) {
            log.error("解析异常消息时失败, 原始异常消息={}", exception, e);
            return fromErrorCode(BaseErrorCallbackCode.SERVER_ERROR);
        }
    }

    /**
     * 解析 BaseException。
     */
    private static ExceptionResolveResult resolveBaseException(BaseException baseException) {
        final BaseCallbackCode defaultCallbackCode = baseException.defaultCallback();
        String formatDefaultMessage = baseException.getDescription();
        // 有些走了自定义异常基类的，但是没有走这个接口赋值，就不会有值，比如throw new BadRequestException("bad_request", "xx不能为空)
        if (!Objects.equals(defaultCallbackCode, baseException.getBaseCallbackCode())) {
            if (Objects.isNull(baseException.getBaseCallbackCode())) {
                formatDefaultMessage = baseException.getDescription();
            } else {
                formatDefaultMessage = baseException.getBaseCallbackCode().getBizMessage();
            }
        }
        // 没有定义资源文件的使用直接使用异常消息，定义了这里会根据异常状态码走i18n资源文件
        // 根据不同异常，有些基于模糊化异常内容的目的，会使用默认状态码去格式化消息
        if (baseException.isMaskErrorDetails()) {
            return new ExceptionResolveResult(defaultCallbackCode.getCode(), defaultCallbackCode.getCode(),
                    defaultCallbackCode.getBizMessage(), baseException.getParams(), baseException.getDescription(),
                    baseException.getExtra());
        }
        return new ExceptionResolveResult(baseException.getCode(), baseException.getCode(), formatDefaultMessage,
                baseException.getParams(), baseException.getDescription(), baseException.getExtra());
    }

    /**
     * 解析 BindException。
     */
    private static ExceptionResolveResult resolveBindException(BindException bindException) {
        final BindingResult result = bindException.getBindingResult();
        final String subMessage = result.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(
                Collectors.joining(";"));
        final String formatCode = result.getAllErrors().isEmpty() ? "" : result.getAllErrors()
                .get(0)
                .getDefaultMessage();
        return new ExceptionResolveResult(BaseErrorCallbackCode.BAD_REQUEST.getCode(), formatCode,
                BaseErrorCallbackCode.BAD_REQUEST.getBizMessage(), new Object[] {}, subMessage, null);
    }

    /**
     * 从错误码创建解析结果。
     */
    private static ExceptionResolveResult fromErrorCode(BaseCallbackCode code) {
        return new ExceptionResolveResult(code.getCode(), code.getCode(), code.getBizMessage(), new Object[] {}, "",
                null);
    }

    /**
     * 异常解析结果。
     *
     * @param exceptionCode 异常码
     * @param formatCode 格式化用的异常码
     * @param formatDefaultMessage 默认格式化消息
     * @param formatParams 格式化参数
     * @param subMessage 子消息
     * @param extra 额外数据
     */
    public record ExceptionResolveResult(String exceptionCode, String formatCode, String formatDefaultMessage,
            Object[] formatParams, String subMessage, Object extra) {
    }
}
