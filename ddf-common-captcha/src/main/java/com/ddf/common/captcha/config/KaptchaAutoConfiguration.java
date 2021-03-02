package com.ddf.common.captcha.config;

import com.ddf.common.captcha.constants.CaptchaConst;
import com.ddf.common.captcha.helper.CaptchaHelper;
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
@EnableConfigurationProperties(KaptchaProperties.class)
public class KaptchaAutoConfiguration {

    private final KaptchaProperties properties;

    public KaptchaAutoConfiguration(KaptchaProperties properties) {
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

    @Bean
    public CaptchaHelper captchaHelper(@Autowired @Qualifier(value = CaptchaConst.KAPTCHA_DEFAULT) DefaultKaptcha defaultKaptcha,
            @Autowired @Qualifier(value = CaptchaConst.KAPTCHA_MATH) DefaultKaptcha mathKaptcha, @Autowired KaptchaProperties kaptchaProperties) {
        return new CaptchaHelper(defaultKaptcha, mathKaptcha, kaptchaProperties);
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
     * 构建属性类
     *
     * @return
     */
    private Properties buildProperties() {
        Properties prop = new Properties();
        // 宽高
        prop.setProperty(Constants.KAPTCHA_IMAGE_WIDTH, String.valueOf(properties.getWidth()));
        prop.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT, String.valueOf(properties.getHeight()));
        // 图片样式
        // 水纹 com.google.code.kaptcha.impl.WaterRipple
        // 鱼眼 com.google.code.kaptcha.impl.FishEyeGimpy
        // 阴影 com.google.code.kaptcha.impl.ShadowGimpy
        prop.setProperty(Constants.KAPTCHA_OBSCURIFICATOR_IMPL, properties.getObscurificator());

        // 文本内容属性
        final KaptchaProperties.Content content = properties.getContent();
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_STRING, content.getSource());
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, String.valueOf(content.getLength()));
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_SPACE, String.valueOf(content.getSpace()));

        // 背景颜色
        KaptchaProperties.BackgroundColor backgroundColor = properties.getBackgroundColor();
        prop.setProperty(Constants.KAPTCHA_BACKGROUND_CLR_FROM, backgroundColor.getFrom());
        prop.setProperty(Constants.KAPTCHA_BACKGROUND_CLR_TO, backgroundColor.getTo());

        // 边框
        KaptchaProperties.Border border = properties.getBorder();
        prop.setProperty(Constants.KAPTCHA_BORDER, border.getEnabled() ? "yes" : "no");
        prop.setProperty(Constants.KAPTCHA_BORDER_COLOR, border.getColor());
        prop.setProperty(Constants.KAPTCHA_BORDER_THICKNESS, String.valueOf(border.getThickness()));

        // 字体
        KaptchaProperties.Font font = properties.getFont();
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_NAMES, font.getName());
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_SIZE, String.valueOf(font.getSize()));
        prop.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_COLOR, font.getColor());

        return prop;
    }

}
