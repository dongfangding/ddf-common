package com.ddf.boot.common.api.support;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * 提供Spring的一些支持，方便别的模块共用
 *
 * @author Snowball
 * @version 1.0
 * @since 2026/03/12 11:27
 */
public class SpringSupport {

    /**
     * 当bean未被定义时，添加注册
     *
     * @param registry Bean 定义注册器
     * @param beanName Bean 名称
     * @param builder 构建器参数
     */
    public static void registerIfAbsent(BeanDefinitionRegistry registry, String beanName,
            BeanDefinitionBuilder builder) {
        if (!registry.containsBeanDefinition(beanName)) {
            registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
        }
    }
}
