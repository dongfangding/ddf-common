package com.ddf.common.captcha.producer;

import com.anji.captcha.service.CaptchaCacheService;
import com.ddf.boot.common.api.model.captcha.CaptchaType;
import com.ddf.boot.common.api.model.captcha.response.CaptchaResult;
import com.ddf.boot.common.core.util.IdsUtil;
import com.ddf.common.captcha.properties.CaptchaProperties;
import com.ddf.common.captcha.properties.KaptchaProperties;
import com.ddf.common.captcha.repository.CacheAdapter;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.springframework.util.FastByteArrayOutputStream;

/**
 * 图形字符验证码生成策略。
 */
public class TextCaptchaProducer implements CaptchaProducer {

    private final DefaultKaptcha defaultKaptcha;
    private final CaptchaProperties captchaProperties;
    private final CaptchaCacheService captchaCacheService;

    public TextCaptchaProducer(DefaultKaptcha defaultKaptcha, CaptchaProperties captchaProperties,
            CaptchaCacheService captchaCacheService) {
        this.defaultKaptcha = defaultKaptcha;
        this.captchaProperties = captchaProperties;
        this.captchaCacheService = captchaCacheService;
    }

    @Override
    public CaptchaType getCaptchaType() {
        return CaptchaType.TEXT;
    }

    @Override
    public CaptchaResult generate() {
        String text = defaultKaptcha.createText();
        BufferedImage image = defaultKaptcha.createImage(text);
        return build(text, text, image);
    }

    private CaptchaResult build(String verifyCode, String cacheValue, BufferedImage image) {
        KaptchaProperties kaptchaProperties = captchaProperties.getKaptcha();
        CaptchaResult result = new CaptchaResult();
        result.setVerifyCode(verifyCode);
        result.setWidth(kaptchaProperties.getWidth());
        result.setHeight(kaptchaProperties.getHeight());
        result.setOriginalImageBase64(encodeImage(image));
        String token = String.format("%s:%s", CacheAdapter.CAPTCHA_KEY_PREFIX, IdsUtil.getUniqueId());
        result.setUuid(token);
        captchaCacheService.set(token, cacheValue, captchaProperties.getKeyExpiredSeconds());
        return result;
    }

    private String encodeImage(BufferedImage image) {
        FastByteArrayOutputStream stream = new FastByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", stream);
        } catch (IOException e) {
            throw new IllegalStateException("验证码生成失败");
        }
        return Base64.getEncoder().encodeToString(stream.toByteArray());
    }
}
