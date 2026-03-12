package com.ddf.boot.common.limit.repeatable.config;

import cn.hutool.core.collection.CollectionUtil;
import com.ddf.boot.common.api.support.SpringSupport;
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
 * @since 2021/02/05 12:56
 */
public class RepeatableRegistrar implements ImportBeanDefinitionRegistrar {

    /**
     * 获取全局注解使用，注册属性类
     *
     * @param metadata metadata参数
     * @param registry Bean 定义注册器
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
		SpringSupport.registerIfAbsent(registry, RepeatableProperties.BEAN_NAME, repeatablePropertiesBuilder);
		SpringSupport.registerIfAbsent(registry, RepeatAspect.BEAN_NAME,
                BeanDefinitionBuilder.genericBeanDefinition(RepeatAspect.class));
		SpringSupport.registerIfAbsent(registry, LocalRepeatableValidator.BEAN_NAME,
                BeanDefinitionBuilder.genericBeanDefinition(LocalRepeatableValidator.class));
		SpringSupport.registerIfAbsent(registry, RedisRepeatableValidator.BEAN_NAME,
                BeanDefinitionBuilder.genericBeanDefinition(RedisRepeatableValidator.class));
    }
}
