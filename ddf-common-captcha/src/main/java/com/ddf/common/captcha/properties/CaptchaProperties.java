package com.ddf.common.captcha.properties;

import com.anji.captcha.config.AjCaptchaAutoConfiguration;
import com.anji.captcha.config.AjCaptchaServiceAutoConfiguration;
import com.anji.captcha.properties.AjCaptchaProperties;
import com.ddf.boot.common.core.util.BeanCopierUtils;
import javax.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * <p>验证码自动注入类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/03/02 15:07
 */
@Data
@ConfigurationProperties(prefix = "customs.captcha")
@AutoConfigureBefore(value = {
        AjCaptchaServiceAutoConfiguration.class, AjCaptchaAutoConfiguration.class})
public class CaptchaProperties implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * 缓存类型redis/local/....
     */
    private com.anji.captcha.properties.AjCaptchaProperties.StorageType cacheType = AjCaptchaProperties.StorageType.redis;

    /**
     * 验证码缓存存在时间，单位秒
     */
    private Integer keyExpiredSeconds = 300;

    /**
     * 基于谷歌Kaptcha的验证码属性类
     * 支持多种字符和数学计算验证码
     */
    private KaptchaProperties kaptcha = new KaptchaProperties();

    /**
     * 基于anji-captcha验证码属性类
     * 支持图片滑动和文字点击方式
     */
    private DefaultAjCaptchaProperties aj = new DefaultAjCaptchaProperties();


    @PostConstruct
    public void initialize() {
        final AjCaptchaProperties ajCaptchaProperties = applicationContext.getBean(AjCaptchaProperties.class);
        // 强制统一走当前模块定义的规则
        BeanCopierUtils.copy(aj, ajCaptchaProperties);
        ajCaptchaProperties.setCacheType(cacheType);
    }

    /**
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
