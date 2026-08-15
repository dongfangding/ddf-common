package com.ddf.common.captcha.producer;

import com.anji.captcha.service.CaptchaCacheService;
import com.ddf.boot.common.api.model.captcha.CaptchaType;
import com.ddf.boot.common.api.model.captcha.response.CaptchaResult;
import com.ddf.common.captcha.event.CaptchaVerifyEvent;
import com.ddf.common.captcha.properties.CaptchaProperties;
import com.ddf.common.captcha.properties.KaptchaProperties;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link TextCaptchaProducer} 扩展点单元测试。
 */
class TextCaptchaProducerTest {

    @Test
    @DisplayName("getCaptchaType 返回 TEXT")
    void shouldReturnTextCaptchaType() {
        TextCaptchaProducer producer = new TextCaptchaProducer(
                mock(DefaultKaptcha.class), mock(CaptchaProperties.class), mock(CaptchaCacheService.class));

        assertThat(producer.getCaptchaType()).isEqualTo(CaptchaType.TEXT);
    }

    @Test
    @DisplayName("generate 返回非空结果且 verifyCode 为 kaptcha 生成文本，缓存被写入")
    void shouldGenerateCaptchaAndWriteCache() {
        DefaultKaptcha kaptcha = mock(DefaultKaptcha.class);
        when(kaptcha.createText()).thenReturn("ABC");
        when(kaptcha.createImage("ABC")).thenReturn(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

        KaptchaProperties kaptchaProperties = mock(KaptchaProperties.class);
        when(kaptchaProperties.getWidth()).thenReturn(100);
        when(kaptchaProperties.getHeight()).thenReturn(40);

        CaptchaProperties captchaProperties = mock(CaptchaProperties.class);
        when(captchaProperties.getKaptcha()).thenReturn(kaptchaProperties);
        when(captchaProperties.getKeyExpiredSeconds()).thenReturn(300);

        CaptchaCacheService captchaCacheService = mock(CaptchaCacheService.class);

        TextCaptchaProducer producer = new TextCaptchaProducer(kaptcha, captchaProperties, captchaCacheService);

        CaptchaResult result = producer.generate();

        assertThat(result).isNotNull();
        assertThat(result.getVerifyCode()).isEqualTo("ABC");
        assertThat(result.getUuid()).startsWith("RUNNING:CAPTCHA:");
        assertThat(result.getWidth()).isEqualTo(100);
        assertThat(result.getHeight()).isEqualTo(40);

        verify(captchaCacheService).set(anyString(), eq("ABC"), eq(300L));
    }

    @Test
    @DisplayName("CaptchaVerifyEvent 正确携带 uuid 与 success")
    void shouldCarryUuidAndSuccessInVerifyEvent() {
        Object source = new Object();
        CaptchaVerifyEvent event = new CaptchaVerifyEvent(source, "uuid-1", true);

        assertThat(event.getSource()).isSameAs(source);
        assertThat(event.getUuid()).isEqualTo("uuid-1");
        assertThat(event.isSuccess()).isTrue();

        CaptchaVerifyEvent failure = new CaptchaVerifyEvent(source, "uuid-2", false);
        assertThat(failure.getUuid()).isEqualTo("uuid-2");
        assertThat(failure.isSuccess()).isFalse();
    }
}
