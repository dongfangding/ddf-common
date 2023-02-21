package com.ddf.boot.common.limit.ratelimit.config;

import cn.hutool.core.collection.CollectionUtil;
import com.ddf.boot.common.limit.ratelimit.annotation.EnableRateLimit;
import com.ddf.boot.common.limit.ratelimit.handler.RateLimitAspect;
import com.ddf.boot.common.limit.ratelimit.keygenerator.GlobalRateLimitKeyGenerator;
import com.ddf.boot.common.limit.ratelimit.keygenerator.IdentityRateLimitKeyGenerator;
import com.ddf.boot.common.limit.ratelimit.keygenerator.IpRateLimitKeyGenerator;
import java.util.Map;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * <p>限流组件注册类</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/05 12:56
 */
public class RateLimitRegistrar implements ImportBeanDefinitionRegistrar {

    /**
     * 获取全局注解使用，注册属性类
     *
     * @param metadata
     * @param registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        if (!metadata.hasAnnotation(EnableRateLimit.class.getName())) {
            return;
        }
        // 填充全局默认限流属性
        final Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableRateLimit.class.getName());
        BeanDefinitionBuilder rateLimitProperties = BeanDefinitionBuilder.genericBeanDefinition(
                RateLimitProperties.class);
        if (CollectionUtil.isNotEmpty(attributes)) {
            attributes.forEach(rateLimitProperties::addPropertyValue);
        }
        // 注册全局属性类
        registry.registerBeanDefinition(RateLimitProperties.BEAN_NAME, rateLimitProperties.getBeanDefinition());

        // 注册限流处理类
        registry.registerBeanDefinition(RateLimitAspect.BEAN_NAME,
                BeanDefinitionBuilder.genericBeanDefinition(RateLimitAspect.class).getBeanDefinition());

        // 注册限流key规则生成器
        registry.registerBeanDefinition(GlobalRateLimitKeyGenerator.BEAN_NAME,
                BeanDefinitionBuilder.genericBeanDefinition(GlobalRateLimitKeyGenerator.class).getBeanDefinition()
        );
        registry.registerBeanDefinition(IdentityRateLimitKeyGenerator.BEAN_NAME,
                BeanDefinitionBuilder.genericBeanDefinition(IdentityRateLimitKeyGenerator.class).getBeanDefinition()
        );
        registry.registerBeanDefinition(IpRateLimitKeyGenerator.BEAN_NAME,
                BeanDefinitionBuilder.genericBeanDefinition(IpRateLimitKeyGenerator.class).getBeanDefinition()
        );
    }
}
