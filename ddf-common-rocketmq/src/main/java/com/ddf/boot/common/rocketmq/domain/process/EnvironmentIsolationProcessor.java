package com.ddf.boot.common.rocketmq.domain.process;

import com.ddf.boot.common.rocketmq.config.RocketEnhanceProperties;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.StringUtils;

public class EnvironmentIsolationProcessor implements BeanPostProcessor {
    private RocketEnhanceProperties rocketEnhanceProperties;
    public EnvironmentIsolationProcessor(RocketEnhanceProperties rocketEnhanceProperties) {
        this.rocketEnhanceProperties = rocketEnhanceProperties;
    }


    /**
     * 在装载Bean之前实现参数修改
     * @param bean 参数
     * @param beanName Bean 名称
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DefaultRocketMQListenerContainer container) {
            if (rocketEnhanceProperties.isEnabledIsolation() && StringUtils.hasText(
                    rocketEnhanceProperties.getEnvironment())) {
                container.setTopic(String.join("_", container.getTopic(), rocketEnhanceProperties.getEnvironment()));
            }
            return container;
        }
        return bean;
    }
}