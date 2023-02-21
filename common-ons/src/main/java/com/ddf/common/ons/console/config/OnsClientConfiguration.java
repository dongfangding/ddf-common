package com.ddf.common.ons.console.config;

import com.aliyun.ons20190214.Client;
import com.aliyun.teaopenapi.models.Config;
import com.ddf.common.ons.console.constant.ConsoleConstants;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.CollectionUtils;

/**
 * <p>初始化Ons多环境客户端</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/17 13:56
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(value = {EnvClientProperties.class})
public class OnsClientConfiguration implements SmartInitializingSingleton, ApplicationContextAware {

    private final EnvClientProperties envClientProperties;

    public OnsClientConfiguration(EnvClientProperties envClientProperties) {
        this.envClientProperties = envClientProperties;
    }

    private ApplicationContext applicationContext;

    @Override
    public void afterSingletonsInstantiated() {
        generateOnsClientBean();
    }

    /**
     * 生成Ons Client相关Bean
     */
    private void generateOnsClientBean() {
        final Map<String, EnvClientProperties.ClientProperties> clients = envClientProperties.getClients();
        if (Objects.isNull(envClientProperties) || CollectionUtils.isEmpty(clients)) {
            log.warn("未配置管理sdk相关初始化参数");
            return;
        }
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
        clients.forEach((env, prop) -> {
            prop.checkRequired();
            String onsClientBeanName = ConsoleConstants.getOnsClientBeanName(env.toUpperCase());
            genericApplicationContext.registerBean(onsClientBeanName, Client.class, () -> {
                Config config = new Config()
                        .setAccessKeyId(prop.getAccessKeyId())
                        .setAccessKeySecret(prop.getAccessKeySecret())
                        .setEndpoint(prop.getEndpoint());
                try {
                    return new Client(config);
                } catch (Exception e) {
                    throw new IllegalStateException(
                            String.format("初始化ONS SDK Client异常， env: %s, props: %s", env, prop), e);
                }
            });
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
