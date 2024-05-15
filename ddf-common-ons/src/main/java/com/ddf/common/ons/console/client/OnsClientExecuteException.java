package com.ddf.common.ons.console.client;

import com.aliyun.tea.TeaException;
import com.ddf.boot.common.api.exception.BaseCallbackCode;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BaseException;
import com.ddf.common.ons.console.model.OnsBizErrorCodeEnum;
import java.util.Objects;

/**
 *
 * ONS 客户端调用异常
 *
 * @author  Jonni Kanerva
 * @since   JDK1.1
 */
public class OnsClientExecuteException extends BaseException {
    static final long serialVersionUID = -1848914673093119416L;

    /**
     * 这里错误码实际对业务含义不大，因此为了简单，定义了通用错误码， 直接返回错误消息，没有对ONS API的错误码进行转换，直接舍弃，用错误消息即可。
     */
    public static final String ONS_COMMON_CODE = "500";

    /**
     * 当前异常默认响应状态码
     *
     * @return
     */
    @Override
    public BaseCallbackCode defaultCallback() {
        return BaseErrorCallbackCode.SERVER_ERROR;
    }

    public OnsClientExecuteException(String message, Throwable cause) {
        super(ONS_COMMON_CODE, message, cause);
    }

    public OnsClientExecuteException(String message) {
        super(ONS_COMMON_CODE, message);
    }

    public OnsClientExecuteException(Throwable cause) {
        super(cause);
    }
    public OnsClientExecuteException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    /**
     * 为适应内部通用错误码拦截功能，简单转换ONS异常类为内部异常类
     *
     * @param cause
     */
    public static OnsClientExecuteException convertTeaException(Throwable cause) {
        return convertTeaException(cause, "");
    }


    /**
     * 为适应内部通用错误码拦截功能，简单转换ONS异常类为内部异常类
     *
     * @param cause
     */
    public static OnsClientExecuteException convertTeaException(Throwable cause, String env) {
        if (cause instanceof TeaException) {
            final String code = ((TeaException) cause).getCode();
            final OnsBizErrorCodeEnum codeEnum = OnsBizErrorCodeEnum.getByValue(code);
            if (Objects.nonNull(codeEnum)) {
                return new OnsClientExecuteException(ONS_COMMON_CODE, env + ": " + codeEnum.getDescription(), cause);
            }
        }
        return new OnsClientExecuteException(cause);
    }
}
