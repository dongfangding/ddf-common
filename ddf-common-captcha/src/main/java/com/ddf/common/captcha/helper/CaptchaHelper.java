package com.ddf.common.captcha.helper;

import com.anji.captcha.model.common.CaptchaTypeEnum;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaCacheService;
import com.anji.captcha.service.CaptchaService;
import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.api.model.captcha.CaptchaType;
import com.ddf.boot.common.api.model.captcha.request.CaptchaCheckRequest;
import com.ddf.boot.common.api.model.captcha.request.CaptchaRequest;
import com.ddf.boot.common.api.model.captcha.request.CaptchaSecondCheckRequest;
import com.ddf.boot.common.api.model.captcha.response.CaptchaCheckResult;
import com.ddf.boot.common.api.model.captcha.response.CaptchaResult;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.core.util.IdsUtil;
import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.common.captcha.constants.CaptchaErrorCode;
import com.ddf.common.captcha.producer.CaptchaProducer;
import com.ddf.common.captcha.producer.MathKaptchaTextCreator;
import com.ddf.common.captcha.properties.CaptchaProperties;
import com.ddf.common.captcha.properties.KaptchaProperties;
import com.ddf.common.captcha.repository.CacheAdapter;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.common.base.Objects;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import javax.imageio.ImageIO;
import org.springframework.util.FastByteArrayOutputStream;

/**
 * <p>验证码生成器帮助类</p>
 *
 * @author Snowball
 * @version 1.0
 * @since 2021/03/02 16:00
 */
public class CaptchaHelper {

    private final DefaultKaptcha defaultKaptcha;

    private final DefaultKaptcha mathKaptcha;

    private final CaptchaProperties captchaProperties;

    private final CaptchaService captchaService;

    private final CaptchaCacheService captchaCacheService;

    private final CacheAdapter cacheAdapter;

    private final Map<CaptchaType, CaptchaProducer> captchaProducerMap;

    /**
     * 校验成功响应码，与 anji-captcha 三方库保持一致。
     */
    private static final String CAPTCHA_SUCCESS_CODE = "0000";

    public CaptchaHelper(DefaultKaptcha defaultKaptcha, DefaultKaptcha mathKaptcha, CaptchaProperties captchaProperties,
            CaptchaService captchaService, CaptchaCacheService captchaCacheService, CacheAdapter cacheAdapter,
            Map<CaptchaType, CaptchaProducer> captchaProducerMap) {
        this.defaultKaptcha = defaultKaptcha;
        this.mathKaptcha = mathKaptcha;
        this.captchaProperties = captchaProperties;
        this.captchaService = captchaService;
        this.captchaCacheService = captchaCacheService;
        this.cacheAdapter = cacheAdapter;
        this.captchaProducerMap = captchaProducerMap;
    }

    /**
     * 生成验证码。
     *
     * @param captchaRequest 参数
     * @return 验证码结果
     */
    public CaptchaResult generate(CaptchaRequest captchaRequest) {
        CaptchaProducer producer = captchaProducerMap.get(captchaRequest.getCaptchaType());
        if (producer != null) {
            return producer.generate();
        }
        // 回退到原有 switch 逻辑（MATH/CLICK_WORDS/PIC_SLIDE 暂未抽取）
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
     * 生成图形验证码。
     *
     * @return 验证码结果
     */
    public CaptchaResult generateText() {
        final String text = defaultKaptcha.createText();
        final BufferedImage image = defaultKaptcha.createImage(text);
        return buildCaptchaResult(text, image, text);
    }

    /**
     * 生成数学表达式验证码。
     *
     * @return 验证码结果
     */
    public CaptchaResult generateMath() {
        final String text = mathKaptcha.createText();
        final MathKaptchaTextCreator.Data parse = MathKaptchaTextCreator.parse(text);
        final BufferedImage image = mathKaptcha.createImage(parse.getCalcCode());
        return buildCaptchaResult(parse.getCalcResult(), image, parse.getCalcResult());
    }

    private CaptchaResult buildCaptchaResult(String verifyCode, BufferedImage image, String cacheValue) {
        final KaptchaProperties kaptchaProperties = captchaProperties.getKaptcha();
        final CaptchaResult result = new CaptchaResult();
        result.setVerifyCode(verifyCode);
        result.setWidth(kaptchaProperties.getWidth());
        result.setHeight(kaptchaProperties.getHeight());
        result.setOriginalImageBase64(encodeImage(image));
        final String token = String.format("%s:%s", CacheAdapter.CAPTCHA_KEY_PREFIX, IdsUtil.getUniqueId());
        result.setUuid(token);
        captchaCacheService.set(token, cacheValue, captchaProperties.getKeyExpiredSeconds());
        return result;
    }

    private String encodeImage(BufferedImage image) {
        final FastByteArrayOutputStream stream = new FastByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", stream);
        } catch (IOException e) {
            throw new IllegalStateException("验证码生成失败");
        }
        return Base64.getEncoder().encodeToString(stream.toByteArray());
    }

    /**
     * 获取文字点选验证码或图片滑块验证码。
     *
     * @param captchaTypeEnum 验证码类型
     * @return 验证码结果
     */
    public CaptchaResult generateAjCaptcha(CaptchaTypeEnum captchaTypeEnum) {
        final CaptchaVO vo = new CaptchaVO();
        vo.setCaptchaType(captchaTypeEnum.getCodeValue());
        final ResponseModel model = captchaService.get(vo);
        final CaptchaVO captchaVO = JsonUtil.toBean(JsonUtil.toJson(model.getRepData()), CaptchaVO.class);
        final CaptchaResult result = new CaptchaResult();
        result.setUuid(captchaVO.getToken());
        result.setOriginalImageBase64(captchaVO.getOriginalImageBase64());
        result.setImageBase64(captchaVO.getJigsawImageBase64());
        result.setWordList(captchaVO.getWordList());
        result.setVerifyCode(captchaVO.getPointJson());
        result.setWidth(310);
        result.setHeight(155);
        return result;
    }

    /**
     * 校验验证码, 这个是给前端调用的
     * 一次校验成功后返回二次校验凭证，二次校验或普通验证码返回空结果。
     *
     * @param request 请求对象
     * @return 校验结果包装对象
     */
    public CaptchaCheckResult check(CaptchaCheckRequest request) {
        final CaptchaType captchaType = request.getCaptchaType();
        if (Objects.equal(CaptchaType.CLICK_WORDS, captchaType) || Objects.equal(CaptchaType.PIC_SLIDE, captchaType)) {
            final CaptchaVO vo = new CaptchaVO();
            vo.setToken(request.getUuid());
            vo.setPointJson(request.getVerifyCode());
            if (CaptchaType.CLICK_WORDS.equals(captchaType)) {
                vo.setCaptchaType(CaptchaTypeEnum.CLICKWORD.getCodeValue());
            } else {
                vo.setCaptchaType(CaptchaTypeEnum.BLOCKPUZZLE.getCodeValue());
            }
            final ResponseModel checkResult = captchaService.check(vo);
            if (!CAPTCHA_SUCCESS_CODE.equals(checkResult.getRepCode())) {
                throw new BusinessException(CaptchaErrorCode.VERIFY_CODE_NOT_MAPPING.getCode(),
                        checkResult.getRepMsg());
            }
        }
        final String captchaVerification = IdsUtil.getUniqueId();
        cacheAdapter.setCaptchaVerification(request.getUuid(), captchaVerification);
        return CaptchaCheckResult.builder().uuid(request.getUuid()).captchaVerification(captchaVerification).build();
    }

    /**
     * 服务端二次校验接口
     *
     * @param request
     */
    public void serverSecondCheck(CaptchaSecondCheckRequest request) {
        final boolean b = cacheAdapter.hasCaptchaVerification(request.getUuid(), request.getCaptchaVerification());
        if (!b) {
            throw new BusinessException(CaptchaErrorCode.VERIFY_CODE_NOT_MAPPING);
        }
    }

    /**
     * 根据 token 获取验证码。
     *
     * @param token token 字符串
     * @return 验证码
     */
    public String getVerifyCodeByToken(String token) {
        return captchaCacheService.get(String.format("%s:%s", CacheAdapter.CAPTCHA_KEY_PREFIX, token));
    }
}
