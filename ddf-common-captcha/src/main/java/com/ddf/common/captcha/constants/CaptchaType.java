package com.ddf.common.captcha.constants;

import com.anji.captcha.model.common.CaptchaTypeEnum;

/**
 * <p>验证码类型</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/07/12 14:33
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

    public CaptchaTypeEnum transferAnJi() {
        if (CaptchaType.CLICK_WORDS.equals(this)) {
            return CaptchaTypeEnum.CLICKWORD;
        }
        return CaptchaTypeEnum.BLOCKPUZZLE;
    }
}
