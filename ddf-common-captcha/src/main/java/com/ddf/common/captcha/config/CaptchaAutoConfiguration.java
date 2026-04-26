package com.ddf.common.captcha.config;

import com.anji.captcha.service.CaptchaCacheService;
import com.anji.captcha.service.CaptchaService;
import com.ddf.boot.common.redis.helper.RedisTemplateHelper;
import com.ddf.common.captcha.constants.CaptchaConst;
import com.ddf.common.captcha.helper.CaptchaHelper;
import com.ddf.common.captcha.producer.MathKaptchaTextCreator;
import com.ddf.common.captcha.properties.CaptchaProperties;
import com.ddf.common.captcha.properties.KaptchaProperties;
import com.ddf.common.captcha.repository.CacheAdapter;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.impl.NoNoise;
import com.google.code.kaptcha.util.Config;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import static com.google.code.kaptcha.Constants.KAPTCHA_TEXTPRODUCER_IMPL;

/**
 * <p>Kaptcha 自动配置类</p>
 *
 * @author Snowball
 * @version 1.0
 * @since 2021/03/02 15:20
 */
@Configuration
@EnableConfigurationProperties(CaptchaProperties.class)
public class CaptchaAutoConfiguration {

    private final CaptchaProperties properties;

    public CaptchaAutoConfiguration(CaptchaProperties properties) {
        this.properties = properties;
    }

    /**
     * 默认验证码实现器
     *
     * @return 实例
     */
    @Bean(name = CaptchaConst.KAPTCHA_DEFAULT)
    public DefaultKaptcha defaultKaptcha() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(new Config(buildProperties()));
        return defaultKaptcha;
    }

    /**
     * 基于数字计算的验证码实现器
     *
     * @return 实例
     */
    @Bean(name = CaptchaConst.KAPTCHA_MATH)
    public DefaultKaptcha mathKaptcha() {
        final Properties properties = buildProperties();
        properties.setProperty(KAPTCHA_TEXTPRODUCER_IMPL, MathKaptchaTextCreator.class.getName());
        properties.setProperty(Constants.KAPTCHA_NOISE_IMPL, NoNoise.class.getName());
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(new Config(properties));
        return defaultKaptcha;
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheAdapter cacheAdapter(StringRedisTemplate stringRedisTemplate,
            RedisTemplateHelper redisTemplateHelper) {
        return new CacheAdapter(stringRedisTemplate, redisTemplateHelper);
    }

    /**
     * 验证码实现帮助类
     *
     * @param defaultKaptcha 默认验证码实例
     * @param mathKaptcha 数学验证码实例
     * @param captchaService 验证码服务实例
     * @param captchaCacheService 验证码缓存服务实例
     * @return 帮助类实例
     */
    @Bean
    @ConditionalOnMissingBean
    public CaptchaHelper captchaHelper(@Qualifier(CaptchaConst.KAPTCHA_DEFAULT) DefaultKaptcha defaultKaptcha,
            @Qualifier(CaptchaConst.KAPTCHA_MATH) DefaultKaptcha mathKaptcha,
            CaptchaService captchaService, CaptchaCacheService captchaCacheService, CacheAdapter cacheAdapter) {
        return new CaptchaHelper(defaultKaptcha, mathKaptcha, properties, captchaService, captchaCacheService, cacheAdapter);
    }

    /**
     * 构建属性类
     *
     * @return 属性
     */
    private Properties buildProperties() {
        Properties prop = new Properties();
        final KaptchaProperties kaptchaProperties = properties.getKaptcha();

        prop.setProperty(Constants.KAPTCHA_IMAGE_WIDTH, String.valueOf(kaptchaProperties.getWidth()));
        prop.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT, String.valueOf(kaptchaProperties.getHeight()));
        prop.setProperty(Constants.KAPTCHA_OBSCURIFICATOR_IMPL, kaptchaProperties.getObscurificator());

        final KaptchaProperties.Content content = kaptchaProperties.getContent();
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_STRING, content.getSource());
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, String.valueOf(content.getLength()));
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_SPACE, String.valueOf(content.getSpace()));

        KaptchaProperties.BackgroundColor backgroundColor = kaptchaProperties.getBackgroundColor();
        prop.setProperty(Constants.KAPTCHA_BACKGROUND_CLR_FROM, backgroundColor.getFrom());
        prop.setProperty(Constants.KAPTCHA_BACKGROUND_CLR_TO, backgroundColor.getTo());

        KaptchaProperties.Border border = kaptchaProperties.getBorder();
        prop.setProperty(Constants.KAPTCHA_BORDER, border.getEnabled() ? "yes" : "no");
        prop.setProperty(Constants.KAPTCHA_BORDER_COLOR, border.getColor());
        prop.setProperty(Constants.KAPTCHA_BORDER_THICKNESS, String.valueOf(border.getThickness()));

        KaptchaProperties.Font font = kaptchaProperties.getFont();
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_NAMES, font.getName());
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_SIZE, String.valueOf(font.getSize()));
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_COLOR, font.getColor());

        return prop;
    }
}
