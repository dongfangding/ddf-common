package com.ddf.common.ons.config;

import com.aliyun.openservices.ons.api.bean.OrderProducerBean;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.ddf.common.ons.console.config.OnsClientConfiguration;
import com.ddf.common.ons.controller.OnsConsoleController;
import com.ddf.common.ons.properties.OnsProperties;
import com.ddf.common.ons.transaction.LocalTransactionCheckerImpl;
import com.ddf.common.ons.transaction.LocalTransactionExecutorImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * ONS自动配置
 *
 * @author snowball
 * @since 2021/8/26 14:48
 **/
@AutoConfiguration
@Import({OnsListenerContainerConfiguration.class, OnsClientConfiguration.class})
public class OnsAutoConfiguration {

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ProducerBean producer(OnsProperties onsProperties) {
        ProducerBean producerBean = new ProducerBean();
        producerBean.setProperties(onsProperties.getOnsProperties());
        return producerBean;
    }

    /**
     * @param onsProperties 参数
     */
    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public OrderProducerBean orderProducer(OnsProperties onsProperties) {
        OrderProducerBean orderProducerBean = new OrderProducerBean();
        orderProducerBean.setProperties(onsProperties.getOnsProperties());
        return orderProducerBean;
    }

    @Bean
    public OnsConsoleController onsConsoleController() {
        return new OnsConsoleController();
    }

    @Bean(name = "localTransactionExecutor")
    public LocalTransactionExecutorImpl localTransactionExecutor() {
        return new LocalTransactionExecutorImpl();
    }

    @Bean(name = "localTransactionChecker")
    public LocalTransactionCheckerImpl localTransactionChecker() {
        return new LocalTransactionCheckerImpl();
    }
}
