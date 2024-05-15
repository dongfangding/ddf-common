package com.ddf.common.ons.config;

import com.aliyun.openservices.ons.api.bean.OrderProducerBean;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.ddf.common.ons.console.config.OnsClientConfiguration;
import com.ddf.common.ons.properties.OnsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * ONS自动配置
 *
 * @author snowball
 * @date 2021/8/26 14:48
 **/
@Configuration
@Import({OnsListenerContainerConfiguration.class, OnsClientConfiguration.class})
@ComponentScan(basePackages = {"com.ddf.common.ons"})
public class OnsAutoConfiguration {

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ProducerBean producer(OnsProperties onsProperties) {
        ProducerBean producerBean = new ProducerBean();
        producerBean.setProperties(onsProperties.getOnsProperties());
        return producerBean;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public OrderProducerBean orderProducer(OnsProperties onsProperties) {
        OrderProducerBean orderProducerBean = new OrderProducerBean();
        orderProducerBean.setProperties(onsProperties.getOnsProperties());
        return orderProducerBean;
    }
}
