package com.ddf.common.captcha.constants;

import com.ddf.boot.common.core.exception200.BaseCallbackCode;
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
    VERIFY_CODE_EXPIRED("CAPTCHA_10004", "验证码已过期"),
    VERIFY_CODE_NOT_MAPPING("CAPTCHA_10005", "验证码错误"),
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
