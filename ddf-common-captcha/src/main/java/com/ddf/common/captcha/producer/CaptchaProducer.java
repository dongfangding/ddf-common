package com.ddf.common.captcha.producer;

import com.ddf.boot.common.api.model.captcha.CaptchaType;
import com.ddf.boot.common.api.model.captcha.response.CaptchaResult;

/**
 * 验证码生成策略接口，接入方注册自定义实现以扩展验证码类型（短信/语音）。
 */
public interface CaptchaProducer {

    /**
     * 支持的验证码类型
     */
    CaptchaType getCaptchaType();

    /**
     * 生成验证码（自包含缓存写入）
     */
    CaptchaResult generate();
}
