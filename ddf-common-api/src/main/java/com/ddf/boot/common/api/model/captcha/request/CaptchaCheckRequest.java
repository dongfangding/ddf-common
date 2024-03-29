package com.ddf.boot.common.api.model.captcha.request;

import com.ddf.boot.common.api.model.captcha.CaptchaType;
import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>验证码校验参数类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/07/12 15:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaptchaCheckRequest implements Serializable {

    private static final long serialVersionUID = 3230860053971294858L;

    /**
     * 获取验证码接口返回的唯一标识
     */
    @NotBlank(message = "uuid不能为空")
    private String uuid;

    /**
     * 验证码结果
     * 如果是普通类型，则传入对应验证码字符
     * 如果是数字计算型，则传入计算结果
     * 如果是文字点选或图片滑动验证码传入坐标信息,对应sdk的pointJson
     */
    @NotBlank(message = "验证码不能为空")
    private String verifyCode;

    /**
     * 验证码类型
     */
    @NotNull(message = "验证码类型不能为空")
    private CaptchaType captchaType;

    /**
     * 是否是二次接口认证，验证码先是前端校验一次，后端会再次校验一次，前端的这个值是false, 后端的必须是true
     */
    private boolean verification;

    /**
     * 二次校验参数值，由前端控件生成
     */
    private String captchaVerification;
}
