package com.ddf.common.captcha.config;


import com.anji.captcha.model.common.CaptchaTypeEnum;
import com.anji.captcha.model.common.Const;
import com.anji.captcha.service.CaptchaService;
import com.anji.captcha.service.impl.CaptchaServiceFactory;
import com.anji.captcha.util.Base64Utils;
import com.anji.captcha.util.ImageUtils;
import com.anji.captcha.util.StringUtils;
import com.ddf.common.captcha.properties.AjCaptchaProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.FileCopyUtils;

@Configuration
@Slf4j
public class AjCaptchaServiceAutoConfiguration {


    private static final AtomicBoolean baseMapInitialized = new AtomicBoolean(false);

    public AjCaptchaServiceAutoConfiguration() {
    }

    /**
     * 注入默认bean以支持想要的默认验证码类型
     *
     * @param prop 配置属性
     * @return
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public CaptchaService captchaService(AjCaptchaProperties prop) {
        return createCaptchaService(prop, null);
    }

    /**
     * 另一个非默认的验证码类型也注册上去，因验证码类型已固定，目前没有采用动态注册
     *
     * @param prop 配置属性
     * @return
     */
    @Bean
    public CaptchaService otherCaptchaService(AjCaptchaProperties prop) {
        CaptchaTypeEnum otherType = Objects.equals(CaptchaTypeEnum.CLICKWORD, prop.getType())
                ? CaptchaTypeEnum.BLOCKPUZZLE : CaptchaTypeEnum.CLICKWORD;
        return createCaptchaService(prop, otherType);
    }

    private CaptchaService createCaptchaService(AjCaptchaProperties prop, CaptchaTypeEnum overrideType) {
        Properties config = buildProperties(prop);
        if ((StringUtils.isNotBlank(prop.getJigsaw()) && prop.getJigsaw().startsWith("classpath:"))
                || (StringUtils.isNotBlank(prop.getPicClick()) && prop.getPicClick().startsWith("classpath:"))) {
            config.put("captcha.init.original", "true");
            if (baseMapInitialized.compareAndSet(false, true)) {
                initializeBaseMap(prop.getJigsaw(), prop.getPicClick());
            }
        }
        if (overrideType != null) {
            config.put(Const.CAPTCHA_TYPE, overrideType.getCodeValue());
        }
        return CaptchaServiceFactory.getInstance(config);
    }
    /**
     * @param prop 配置属性
     */
    private Properties buildProperties(AjCaptchaProperties prop) {
        log.info("自定义配置项：{}", prop.toString());
        Properties config = new Properties();
        config.put("captcha.cacheType", prop.getCacheType().name());
        config.put("captcha.water.mark", prop.getWaterMark());
        config.put("captcha.font.type", prop.getFontType());
        config.put("captcha.type", prop.getType().getCodeValue());
        config.put("captcha.interference.options", prop.getInterferenceOptions());
        config.put("captcha.captchaOriginalPath.jigsaw", prop.getJigsaw());
        config.put("captcha.captchaOriginalPath.pic-click", prop.getPicClick());
        config.put("captcha.slip.offset", prop.getSlipOffset());
        config.put("captcha.aes.status", String.valueOf(prop.getAesStatus()));
        config.put("captcha.water.font", prop.getWaterFont());
        config.put("captcha.cache.number", prop.getCacheNumber());
        config.put("captcha.timing.clear", prop.getTimingClear());
        config.put("captcha.history.data.clear.enable", prop.isHistoryDataClearEnable() ? "1" : "0");
        config.put("captcha.req.frequency.limit.enable", prop.isReqFrequencyLimitEnable() ? "1" : "0");
        config.put("captcha.req.get.lock.limit", prop.getReqGetLockLimit() + "");
        config.put("captcha.req.get.lock.seconds", prop.getReqGetLockSeconds() + "");
        config.put("captcha.req.get.minute.limit", prop.getReqGetMinuteLimit() + "");
        config.put("captcha.req.check.minute.limit", prop.getReqCheckMinuteLimit() + "");
        config.put("captcha.req.verify.minute.limit", prop.getReqVerifyMinuteLimit() + "");
        return config;
    }
    /**
     * @param jigsaw 参数
     * @param picClick 参数
     */
    private static void initializeBaseMap(String jigsaw, String picClick) {
        ImageUtils.cacheBootImage(getResourcesImagesFile(jigsaw + "/original/*.png"), getResourcesImagesFile(jigsaw + "/slidingBlock/*.png"), getResourcesImagesFile(picClick + "/*.png"));
    }
    /**
     * @param path 参数
     */
    public static Map<String, String> getResourcesImagesFile(String path) {
        Map<String, String> imgMap = new HashMap<>();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        try {
            Resource[] resources = resolver.getResources(path);
            for (Resource resource : resources) {
                byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
                String base64 = Base64Utils.encodeToString(bytes);
                String filename = resource.getFilename();
                imgMap.put(filename, base64);
            }
        } catch (Exception e) {
            log.error("加载验证码图片资源失败, path={}", path, e);
        }

        return imgMap;
    }
}