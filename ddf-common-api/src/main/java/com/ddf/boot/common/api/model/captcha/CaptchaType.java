package com.ddf.boot.common.api.model.captcha;

/**
 * <p>验证码类型</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2021/07/12 14:33
 */
public enum CaptchaType {

    /**
     * 文本
     */
    TEXT,

    /**
     * 数字计算
     */
    MATH,

    /**
     * 文字点击
     */
    CLICK_WORDS,

    /**
     * 图片滑动
     */
    PIC_SLIDE

    ;
}
