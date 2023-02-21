package com.ddf.boot.common.api.model.captcha.request;

import com.ddf.boot.common.api.model.captcha.CaptchaType;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>验证码请求类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/07/12 14:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaptchaRequest implements Serializable {

    private static final long serialVersionUID = -7363856377652909090L;

    /**
     * 验证码类型
     */
    private CaptchaType captchaType = CaptchaType.MATH;

}
