package com.ddf.common.captcha.config;

import com.anji.captcha.service.CaptchaCacheService;
import com.anji.captcha.service.CaptchaService;
import com.ddf.common.captcha.constants.CaptchaConst;
import com.ddf.common.captcha.helper.CaptchaHelper;
import com.ddf.common.captcha.producer.AnjiCaptchaCacheService;
import com.ddf.common.captcha.properties.CaptchaProperties;
import com.ddf.common.captcha.properties.KaptchaProperties;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.google.code.kaptcha.Constants.KAPTCHA_TEXTPRODUCER_IMPL;


/**
 * <p>Kaptcha自动配置类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/03/02 15:20
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
     * @return
     */
    @Bean(name = CaptchaConst.KAPTCHA_DEFAULT)
    public DefaultKaptcha defaultKaptcha() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(new Config(buildProperties()));
        return defaultKaptcha;
    }

    /**
     * 基于数字计算的验证码实现其
     *
     * @return
     */
    @Bean(name = CaptchaConst.KAPTCHA_MATH)
    public DefaultKaptcha mathKaptcha() {
        final Properties properties = buildProperties();
        // 验证码文本生成器
        properties.setProperty(KAPTCHA_TEXTPRODUCER_IMPL, "com.ddf.common.captcha.producer.MathKaptchaTextCreator");
        // 干扰实现类, 数学计算无干扰线
        properties.setProperty(Constants.KAPTCHA_NOISE_IMPL, "com.google.code.kaptcha.impl.NoNoise");
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(new Config(properties));
        return defaultKaptcha;
    }

    /**
     * 验证码实现帮助类
     *
     * @param defaultKaptcha
     * @param mathKaptcha
     * @param captchaService
     * @return
     */
    @Bean
    public CaptchaHelper captchaHelper(@Autowired @Qualifier(value = CaptchaConst.KAPTCHA_DEFAULT) DefaultKaptcha defaultKaptcha,
            @Autowired @Qualifier(value = CaptchaConst.KAPTCHA_MATH) DefaultKaptcha mathKaptcha,
            @Autowired CaptchaService captchaService) {
        return new CaptchaHelper(defaultKaptcha, mathKaptcha, properties, captchaService);
    }


    /**
     * 三方验证码缓存实现
     *
     * @return
     */
    @Bean
    public CaptchaCacheService anjiCaptchaCacheService() {
        return new AnjiCaptchaCacheService();
    }

    /**
     * 构建属性类
     *
     * @return
     */
    private Properties buildProperties() {
        Properties prop = new Properties();
        final KaptchaProperties kaptchaProperties = properties.getKaptcha();

        // 宽高
        prop.setProperty(Constants.KAPTCHA_IMAGE_WIDTH, String.valueOf(kaptchaProperties.getWidth()));
        prop.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT, String.valueOf(kaptchaProperties.getHeight()));
        // 图片样式
        // 水纹 com.google.code.kaptcha.impl.WaterRipple
        // 鱼眼 com.google.code.kaptcha.impl.FishEyeGimpy
        // 阴影 com.google.code.kaptcha.impl.ShadowGimpy
        prop.setProperty(Constants.KAPTCHA_OBSCURIFICATOR_IMPL, kaptchaProperties.getObscurificator());

        // 文本内容属性
        final KaptchaProperties.Content content = kaptchaProperties.getContent();
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_STRING, content.getSource());
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, String.valueOf(content.getLength()));
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_SPACE, String.valueOf(content.getSpace()));

        // 背景颜色
        KaptchaProperties.BackgroundColor backgroundColor = kaptchaProperties.getBackgroundColor();
        prop.setProperty(Constants.KAPTCHA_BACKGROUND_CLR_FROM, backgroundColor.getFrom());
        prop.setProperty(Constants.KAPTCHA_BACKGROUND_CLR_TO, backgroundColor.getTo());

        // 边框
        KaptchaProperties.Border border = kaptchaProperties.getBorder();
        prop.setProperty(Constants.KAPTCHA_BORDER, border.getEnabled() ? "yes" : "no");
        prop.setProperty(Constants.KAPTCHA_BORDER_COLOR, border.getColor());
        prop.setProperty(Constants.KAPTCHA_BORDER_THICKNESS, String.valueOf(border.getThickness()));

        // 字体
        KaptchaProperties.Font font = kaptchaProperties.getFont();
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_NAMES, font.getName());
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_SIZE, String.valueOf(font.getSize()));
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_COLOR, font.getColor());

        return prop;
    }

}
