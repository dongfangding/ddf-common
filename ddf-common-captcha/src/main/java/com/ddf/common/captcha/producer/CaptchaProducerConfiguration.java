package com.ddf.common.captcha.producer;

import com.ddf.boot.common.api.model.captcha.CaptchaType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 聚合所有 {@link CaptchaProducer} 到类型分发 Map。
 */
@Configuration
public class CaptchaProducerConfiguration {

    @Bean
    public Map<CaptchaType, CaptchaProducer> captchaProducerMap(List<CaptchaProducer> producers) {
        return producers.stream().collect(Collectors.toMap(CaptchaProducer::getCaptchaType, p -> p));
    }
}
