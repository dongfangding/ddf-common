package com.ddf.common.ons.config;

import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.batch.BatchMessageListener;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.ddf.common.ons.annotation.OnsMessageListener;
import com.ddf.common.ons.enume.ConsumeMode;
import com.ddf.common.ons.enume.MessageModel;
import com.ddf.common.ons.listener.BaseOnsListenerContainer;
import com.ddf.common.ons.properties.OnsProperties;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;

/**
 * ONS监听器容器配置， 根据{@link OnsMessageListener} 构建出监听容器类
 * 
 * @author snowball
 * @date 2021/8/26 14:24
 **/
@Configuration
@EnableConfigurationProperties(value = {OnsProperties.class})
public class OnsListenerContainerConfiguration implements ApplicationContextAware, SmartInitializingSingleton {

    private final static Logger LOGGER = LoggerFactory.getLogger(OnsListenerContainerConfiguration.class);

    private ConfigurableApplicationContext applicationContext;

    private final AtomicLong counter = new AtomicLong(0);

    private final OnsProperties onsProperties;

    public OnsListenerContainerConfiguration(Environment environment, OnsProperties onsProperties) {
        this.onsProperties = onsProperties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(OnsMessageListener.class)
                .entrySet().stream().filter(entry -> !ScopedProxyUtils.isScopedTarget(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        beans.forEach(this::registerListenerContainer);
    }

    private void registerListenerContainer(String beanName, Object bean) {
        Class<?> clazz = AopProxyUtils.ultimateTargetClass(bean);

        if (!OnsMessageListener.class.isAssignableFrom(bean.getClass())) {
            throw new IllegalStateException(clazz + " is not instance of " + OnsMessageListener.class.getName());
        }

        if (MessageListener.class.isAssignableFrom(bean.getClass()) && BatchMessageListener.class.isAssignableFrom(bean.getClass())
                && MessageOrderListener.class.isAssignableFrom(bean.getClass())) {
            throw new IllegalStateException(clazz + " cannot be both instance of " + MessageListener.class.getName()
                    + " or " + BatchMessageListener.class.getName() + " or " + MessageOrderListener.class.getName());
        }

        if (!MessageListener.class.isAssignableFrom(bean.getClass()) && !BatchMessageListener.class.isAssignableFrom(bean.getClass())
                && !MessageOrderListener.class.isAssignableFrom(bean.getClass())) {
            throw new IllegalStateException(clazz + " is not instance of " + MessageListener.class.getName()
                    + " or " + BatchMessageListener.class.getName() + " or " + MessageOrderListener.class.getName());
        }

        OnsMessageListener annotation = clazz.getAnnotation(OnsMessageListener.class);

        String containerBeanName = String.format("%s#%s", BaseOnsListenerContainer.class.getName(),
                counter.incrementAndGet());

        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
        genericApplicationContext.registerBean(containerBeanName, BaseOnsListenerContainer.class,
                () -> createOnsListenerContainer(annotation, bean, containerBeanName));
        BaseOnsListenerContainer container = genericApplicationContext.getBean(containerBeanName,
                BaseOnsListenerContainer.class);

        if (!container.isRunning()) {
            try {
                container.start();
            } catch (Exception e) {
                LOGGER.error("Started container failed. {}", container, e);
                throw new RuntimeException(e);
            }
        }
        LOGGER.info("Register the Listener:[{}] to the Container:[{}]", beanName, containerBeanName);
    }

    private BaseOnsListenerContainer createOnsListenerContainer(OnsMessageListener annotation,
                                                                   Object bean, String beanName) {
        BaseOnsListenerContainer container = new BaseOnsListenerContainer();
        container.setOnsMessageListener((OnsMessageListener) bean);
        container.setAnnotation(annotation);
        container.setAccessKey(onsProperties.getAccessKey());
        container.setSecretKey(onsProperties.getSecretKey());
        container.setNameServerAddr(onsProperties.getNameServerAddr());

        if (MessageListener.class.isAssignableFrom(bean.getClass())) {
            container.setMessageListener((MessageListener) bean);
            container.setConsumeMode(ConsumeMode.SERIAL);
        } else if (BatchMessageListener.class.isAssignableFrom(bean.getClass())) {
            container.setBatchMessageListener((BatchMessageListener) bean);
            container.setConsumeMode(ConsumeMode.BATCH);
        } else if (MessageOrderListener.class.isAssignableFrom(bean.getClass())) {
            if (annotation.messageModel() == MessageModel.BROADCASTING) {
                throw new BeanDefinitionValidationException(
                        "Bad annotation definition in @OnsMessageListener, MessageModel BROADCASTING does not support ORDERLY message");
            }
            container.setMessageOrderListener((MessageOrderListener) bean);
            container.setConsumeMode(ConsumeMode.ORDERLY);
        } else {
            throw new BeanDefinitionValidationException("the Class modifier by Annotation @OnsMessageListener must implements interface MessageListener or BatchMessageListener or MessageOrderListener ");
        }

        container.setName(beanName);

        return container;
    }

}
