package com.ddf.boot.common.core.repeatable;

import cn.hutool.core.collection.CollectionUtil;
import java.util.Map;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * <p>防重提交配置类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/02/05 12:56
 */
public class RepeatableRegistrar implements ImportBeanDefinitionRegistrar {

    /**
     * 获取全局注解使用，注册属性类
     *
     * @param metadata
     * @param registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        boolean exist = metadata.hasAnnotation(EnableRepeatable.class.getName());
        if (!exist) {
            return;
        }
        final Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableRepeatable.class.getName());
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RepeatableProperties.class);
        if (CollectionUtil.isNotEmpty(attributes)) {
            attributes.forEach(builder::addPropertyValue);
        }
        registry.registerBeanDefinition("repeatableProperties", builder.getBeanDefinition());
        registry.registerBeanDefinition("repeatAspect",
                BeanDefinitionBuilder.genericBeanDefinition(RepeatAspect.class).getBeanDefinition());
        registry.registerBeanDefinition(LocalRepeatableValidator.BEAN_NAME,
                BeanDefinitionBuilder.genericBeanDefinition(LocalRepeatableValidator.class).getBeanDefinition());
    }
}
