package com.ddf.boot.common.ext.constants;

import com.ddf.boot.common.api.exception.BaseCallbackCode;
import lombok.Getter;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/05/21 21:40
 */
public enum ExceptionCode implements BaseCallbackCode {
    /**
     * 异常状态定义
     */
    SMS_SEND_FAILURE("sms_send_failure", "短信发送失败")

    ;
    @Getter
    private final String code;

    @Getter
    private final String description;

    ExceptionCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
