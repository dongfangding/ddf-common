package com.ddf.common.captcha.constants;

import com.ddf.boot.common.api.exception.BaseCallbackCode;
import lombok.Getter;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/10/13 16:59
 */
public enum CaptchaErrorCode implements BaseCallbackCode {
    /**
     * 验证码异常
     */
    VERIFY_CODE_EXPIRED("verify_code_expired", "验证码已过期"),
    VERIFY_CODE_NOT_MAPPING("verify_code_not_mapping", "验证码错误"),
    ;


    CaptchaErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Getter
    private final String code;

    @Getter
    private final String description;
}
