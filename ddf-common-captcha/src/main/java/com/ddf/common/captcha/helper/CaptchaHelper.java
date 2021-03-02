package com.ddf.common.captcha.helper;

import com.ddf.common.captcha.model.CaptchaResult;
import com.ddf.common.captcha.producer.MathKaptchaTextCreator;
import com.ddf.common.captcha.properties.KaptchaProperties;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import org.springframework.util.FastByteArrayOutputStream;

/**
 * <p>验证码生成器帮助类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/03/02 16:00
 */
@AllArgsConstructor
public class CaptchaHelper {

    private DefaultKaptcha defaultKaptcha;

    private DefaultKaptcha mathKaptcha;

    private KaptchaProperties kaptchaProperties;

    /**
     * 生成图形验证码
     *
     * @return
     * @throws IOException
     */
    public CaptchaResult generateText() {
        final CaptchaResult result = new CaptchaResult();
        final String text = defaultKaptcha.createText();
        result.setVerifyCode(text);
        result.setWidth(kaptchaProperties.getWidth());
        result.setHeight(kaptchaProperties.getHeight());
        final BufferedImage image = defaultKaptcha.createImage(text);
        final FastByteArrayOutputStream stream = new FastByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", stream);
        } catch (IOException e) {
            throw new IllegalStateException("验证码生成失败");
        }
        result.setImageBase64(Base64.getEncoder().encodeToString(stream.toByteArray()));
        return result;
    }

    /**
     * 生成数学表达式验证码
     *
     * @return
     * @throws IOException
     */
    public CaptchaResult generateMath() {
        final String text = mathKaptcha.createText();
        final MathKaptchaTextCreator.Data parse = MathKaptchaTextCreator.parse(text);

        final CaptchaResult result = new CaptchaResult();
        result.setVerifyCode(parse.getCalcResult());
        result.setWidth(kaptchaProperties.getWidth());
        result.setHeight(kaptchaProperties.getHeight());
        final BufferedImage image = defaultKaptcha.createImage(parse.getCalcCode());
        final FastByteArrayOutputStream stream = new FastByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", stream);
        } catch (IOException e) {
            throw new IllegalStateException("验证码生成失败");
        }
        result.setImageBase64(Base64.getEncoder().encodeToString(stream.toByteArray()));
        return result;

    }

}
