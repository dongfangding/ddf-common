package com.ddf.boot.common.limit.repeatable.config;

import cn.hutool.core.collection.CollectionUtil;
import com.ddf.boot.common.limit.repeatable.annotation.EnableRepeatable;
import com.ddf.boot.common.limit.repeatable.handler.RepeatAspect;
import com.ddf.boot.common.limit.repeatable.validator.LocalRepeatableValidator;
import com.ddf.boot.common.limit.repeatable.validator.RedisRepeatableValidator;
import java.util.Map;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * <p>防重提交配置类</p >
 *
 * @author dongfang.ding
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
        if (!metadata.hasAnnotation(EnableRepeatable.class.getName())) {
            return;
        }

        final Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableRepeatable.class.getName());
        BeanDefinitionBuilder repeatablePropertiesBuilder = BeanDefinitionBuilder.genericBeanDefinition(
                RepeatableProperties.class);
        if (CollectionUtil.isNotEmpty(attributes)) {
            attributes.forEach(repeatablePropertiesBuilder::addPropertyValue);
        }
        registry.registerBeanDefinition(RepeatableProperties.BEAN_NAME, repeatablePropertiesBuilder.getBeanDefinition());
        registry.registerBeanDefinition(
                RepeatAspect.BEAN_NAME, BeanDefinitionBuilder.genericBeanDefinition(RepeatAspect.class)
                        .getBeanDefinition());
        registry.registerBeanDefinition(
                LocalRepeatableValidator.BEAN_NAME,
                BeanDefinitionBuilder.genericBeanDefinition(LocalRepeatableValidator.class)
                        .getBeanDefinition()
        );
        registry.registerBeanDefinition(
                RedisRepeatableValidator.BEAN_NAME,
                BeanDefinitionBuilder.genericBeanDefinition(RedisRepeatableValidator.class)
                        .getBeanDefinition()
        );
    }
}
