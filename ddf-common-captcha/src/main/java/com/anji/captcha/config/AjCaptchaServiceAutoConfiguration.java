//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.anji.captcha.config;

import com.anji.captcha.model.common.CaptchaTypeEnum;
import com.anji.captcha.model.common.Const;
import com.anji.captcha.properties.AjCaptchaProperties;
import com.anji.captcha.service.CaptchaService;
import com.anji.captcha.service.impl.CaptchaServiceFactory;
import com.anji.captcha.util.ImageUtils;
import com.anji.captcha.util.StringUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;


/**
 *
 * 更改原实现，允许同时存在两个验证码实现类，这样可以通过外部参数处理同时支持两种验证码
 *
 * @author snowball
 * @date 2022/9/2 18:53
 **/
@Configuration
public class AjCaptchaServiceAutoConfiguration {
    private static Logger logger = LoggerFactory.getLogger(AjCaptchaServiceAutoConfiguration.class);

    public AjCaptchaServiceAutoConfiguration() {
    }

    /**
     * 注入默认bean以支持想要的默认验证码类型
     *
     * @param prop
     * @return
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public CaptchaService captchaService(AjCaptchaProperties prop) {
        Properties config = buildProperties(prop);
        if (StringUtils.isNotBlank(prop.getJigsaw()) && prop.getJigsaw().startsWith("classpath:") || StringUtils.isNotBlank(prop.getPicClick()) && prop.getPicClick().startsWith("classpath:")) {
            config.put("captcha.init.original", "true");
            initializeBaseMap(prop.getJigsaw(), prop.getPicClick());
        }
        return CaptchaServiceFactory.getInstance(config);
    }


    /**
     * 另一个非默认的验证码类型也注册上去，因验证码类型已固定，目前没有采用动态注册
     *
     * @param prop
     * @return
     */
    @Bean
    public CaptchaService otherCaptchaService(AjCaptchaProperties prop) {
        Properties config = buildProperties(prop);
        if (StringUtils.isNotBlank(prop.getJigsaw()) && prop.getJigsaw().startsWith("classpath:") || StringUtils.isNotBlank(prop.getPicClick()) && prop.getPicClick().startsWith("classpath:")) {
            config.put("captcha.init.original", "true");
            initializeBaseMap(prop.getJigsaw(), prop.getPicClick());
        }
        config.put(Const.CAPTCHA_TYPE, Objects.equals(CaptchaTypeEnum.CLICKWORD, prop.getType()) ?
                CaptchaTypeEnum.BLOCKPUZZLE.getCodeValue() : CaptchaTypeEnum.CLICKWORD.getCodeValue());
        return CaptchaServiceFactory.getInstance(config);
    }

    private Properties buildProperties(AjCaptchaProperties prop) {
        logger.info("自定义配置项：{}", prop.toString());
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
        config.put("captcha.req.frequency.limit.enable", prop.getReqFrequencyLimitEnable() ? "1" : "0");
        config.put("captcha.req.get.lock.limit", prop.getReqGetLockLimit() + "");
        config.put("captcha.req.get.lock.seconds", prop.getReqGetLockSeconds() + "");
        config.put("captcha.req.get.minute.limit", prop.getReqGetMinuteLimit() + "");
        config.put("captcha.req.check.minute.limit", prop.getReqCheckMinuteLimit() + "");
        config.put("captcha.req.verify.minute.limit", prop.getReqVerifyMinuteLimit() + "");
        return config;
    }

    private static void initializeBaseMap(String jigsaw, String picClick) {
        ImageUtils.cacheBootImage(getResourcesImagesFile(jigsaw + "/original/*.png"), getResourcesImagesFile(jigsaw + "/slidingBlock/*.png"), getResourcesImagesFile(picClick + "/*.png"));
    }

    public static Map<String, String> getResourcesImagesFile(String path) {
        Map<String, String> imgMap = new HashMap();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        try {
            Resource[] resources = resolver.getResources(path);
            Resource[] var4 = resources;
            int var5 = resources.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                Resource resource = var4[var6];
                byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
                String string = Base64Utils.encodeToString(bytes);
                String filename = resource.getFilename();
                imgMap.put(filename, string);
            }
        } catch (Exception var11) {
            var11.printStackTrace();
        }

        return imgMap;
    }
}
