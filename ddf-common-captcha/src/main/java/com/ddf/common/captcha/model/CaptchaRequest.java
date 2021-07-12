package com.ddf.common.captcha.model;

import com.ddf.common.captcha.constants.CaptchaType;
import java.io.Serializable;
import lombok.Data;

/**
 * <p>验证码请求类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/07/12 14:32
 */
@Data
public class CaptchaRequest implements Serializable {

    /**
     * 验证码类型
     */
    private CaptchaType captchaType = CaptchaType.MATH;

}
