package com.ddf.boot.common.core.constant;

import com.ddf.boot.common.api.exception.BaseCallbackCode;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2023/06/26 22:16
 */
public enum CoreExceptionCode implements BaseCallbackCode {

    ILLEGAL_TOKEN("ILLEGAL_TOKEN", "token格式不合法"),
    FORGE_TOKEN("FORGE_TOKEN", "伪造身份信息"),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "已过期的登录信息，请重新登录")

    ;

    CoreExceptionCode(String code, String description) {
        this.code = code;
        this.description = description;
        this.bizMessage = description;
    }

    CoreExceptionCode(String code, String description, String bizMessage) {
        this.code = code;
        this.description = description;
        this.bizMessage = bizMessage;
    }



    private final String code;
    private final String description;
    private final String bizMessage;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getBizMessage() {
        return bizMessage;
    }
}
