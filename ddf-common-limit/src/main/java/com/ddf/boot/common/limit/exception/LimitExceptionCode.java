package com.ddf.boot.common.limit.exception;

import com.ddf.boot.common.core.exception200.BaseCallbackCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/24 13:52
 */
@AllArgsConstructor
public enum LimitExceptionCode implements BaseCallbackCode {

    /**
     * 异常码
     */
    RATE_LIMIT("999", "接口已限流"),

    REPEAT_SUBMIT("998", "操作频繁"),

    ;


    /**
     * 异常code码
     */
    @Getter
    private final String code;

    /**
     * 异常消息
     */
    @Getter
    private final String description;
}
