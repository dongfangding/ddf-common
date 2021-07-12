package com.ddf.common.captcha.model;

import java.io.Serializable;
import lombok.Data;

/**
 * <p>验证码校验参数类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/07/12 15:16
 */
@Data
public class CaptchaCheckRequest implements Serializable {

    /**
     * 获取验证码接口返回的token
     */
    private String token;

    /**
     * 验证码结果
     * 如果是普通类型，则传入对应验证码字符
     * 如果是数字计算型，则传入计算结果
     * 如果是文字点选或图片滑动验证码传入坐标信息,对应sdk的pointJson
     */
    private String verifyCode;

    /**
     * 验证码类型, 当为文字点选或图片滑动验证码时必传
     * blockPuzzle 滑动拼图
     * clickWord   文字点选
     */
    private String captchaType;
}
