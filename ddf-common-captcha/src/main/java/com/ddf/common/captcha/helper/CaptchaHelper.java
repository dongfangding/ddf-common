package com.ddf.common.captcha.helper;

import com.anji.captcha.model.common.CaptchaTypeEnum;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaCacheService;
import com.anji.captcha.service.CaptchaService;
import com.anji.captcha.service.impl.CaptchaServiceFactory;
import com.ddf.boot.common.core.exception200.BusinessException;
import com.ddf.boot.common.core.util.IdsUtil;
import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.common.captcha.constants.CaptchaErrorCode;
import com.ddf.common.captcha.model.CaptchaCheckRequest;
import com.ddf.common.captcha.model.CaptchaRequest;
import com.ddf.common.captcha.model.CaptchaResult;
import com.ddf.common.captcha.producer.MathKaptchaTextCreator;
import com.ddf.common.captcha.properties.CaptchaProperties;
import com.ddf.common.captcha.properties.KaptchaProperties;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.common.base.Objects;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.springframework.util.FastByteArrayOutputStream;

/**
 * <p>验证码生成器帮助类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/03/02 16:00
 */
public class CaptchaHelper {

    private DefaultKaptcha defaultKaptcha;

    private DefaultKaptcha mathKaptcha;

    private CaptchaProperties captchaProperties;

    private CaptchaService captchaService;

    private CaptchaCacheService captchaCacheService;

    public final static String CAPTCHA_KEY_PREFIX = "RUNNING:CAPTCHA:";

    public CaptchaHelper(DefaultKaptcha defaultKaptcha, DefaultKaptcha mathKaptcha, CaptchaProperties captchaProperties, CaptchaService captchaService) {
        this.defaultKaptcha = defaultKaptcha;
        this.mathKaptcha = mathKaptcha;
        this.captchaProperties = captchaProperties;
        this.captchaService = captchaService;
        captchaCacheService = CaptchaServiceFactory.getCache(captchaProperties.getCacheType().name());
    }

    /**
     * 生成验证码
     *
     * @return
     */
    public CaptchaResult generate(CaptchaRequest captchaRequest) {
        switch (captchaRequest.getCaptchaType()) {
            case TEXT:
                return generateText();
            case MATH:
                return generateMath();
            case CLICK_WORDS:
                return generateAjCaptcha(CaptchaTypeEnum.CLICKWORD);
            case PIC_SLIDE:
                return generateAjCaptcha(CaptchaTypeEnum.BLOCKPUZZLE);
            default:
                return generateMath();
        }
    }


    /**
     * 生成图形验证码
     *
     * @return
     * @throws IOException
     */
    public CaptchaResult generateText() {
        final KaptchaProperties kaptchaProperties = captchaProperties.getKaptcha();
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
        final String token = CAPTCHA_KEY_PREFIX + IdsUtil.getNextStrId();
        result.setToken(token);
        captchaCacheService.set(token, text, captchaProperties.getKeyExpiredSeconds());
        return result;
    }

    /**
     * 生成数学表达式验证码
     *
     * @return
     * @throws IOException
     */
    public CaptchaResult generateMath() {
        final KaptchaProperties kaptchaProperties = captchaProperties.getKaptcha();
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
        final String token = CAPTCHA_KEY_PREFIX + IdsUtil.getNextStrId();
        result.setToken(token);
        captchaCacheService.set(token, parse.getCalcResult(), captchaProperties.getKeyExpiredSeconds());
        return result;
    }

    /**
     * 获取文字点击验证码/获取图片滑块验证码
     *
     * @return
     */
    public CaptchaResult generateAjCaptcha(CaptchaTypeEnum captchaTypeEnum) {
        final CaptchaVO vo = new CaptchaVO();
        vo.setCaptchaType(captchaTypeEnum.getCodeValue());
        final ResponseModel model = captchaService.get(vo);
        final CaptchaVO captchaVO = JsonUtil.toBean(JsonUtil.toJson(model.getRepData()), CaptchaVO.class);
        final CaptchaResult result = new CaptchaResult();
        result.setToken(captchaVO.getToken());
        // 底图base64编码
        result.setImageBase64(captchaVO.getOriginalImageBase64());
        // 滑块图base64编码
        result.setJigsawImageBase64(captchaVO.getJigsawImageBase64());
        result.setWordList(captchaVO.getWordList());
        result.setVerifyCode(captchaVO.getPointJson());
        return result;
    }


    /**
     * 校验验证码
     */
    public boolean check(CaptchaCheckRequest request) {
        final String captchaType = request.getCaptchaType();
        if (Objects.equal(CaptchaTypeEnum.CLICKWORD.getCodeValue(), captchaType) ||
                Objects.equal(CaptchaTypeEnum.BLOCKPUZZLE.getCodeValue(), captchaType)) {
            final CaptchaVO vo = new CaptchaVO();
            vo.setToken(request.getToken());
            vo.setPointJson(request.getVerifyCode());
            vo.setCaptchaType(captchaType);
            final ResponseModel checkResult = captchaService.check(vo);
            if (!"0000".equals(checkResult.getRepCode())) {
                throw new BusinessException(CaptchaErrorCode.VERIFY_CODE_NOT_MAPPING.getCode(), checkResult.getRepMsg());
            }
        } else {
            final String verifyCode = captchaCacheService.get(request.getToken());
            PreconditionUtil.checkArgument(java.util.Objects.nonNull(verifyCode), CaptchaErrorCode.VERIFY_CODE_EXPIRED);
            PreconditionUtil.checkArgument(
                    java.util.Objects.equals(verifyCode, request.getVerifyCode()), CaptchaErrorCode.VERIFY_CODE_NOT_MAPPING);
        }
        return Boolean.TRUE;
    }

    /**
     * 获取验证码
     *
     * @param token
     * @return
     */
    public String getVerifyCodeByToken(String token) {
        return captchaCacheService.get(CAPTCHA_KEY_PREFIX + token);
    }
}
