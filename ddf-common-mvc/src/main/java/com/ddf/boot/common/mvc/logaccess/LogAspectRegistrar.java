package com.ddf.boot.common.mvc.logaccess;

import com.ddf.boot.common.api.support.SpringSupport;
import java.util.Map;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 注册日志记录相关bean
 *
 * @author dongfang.ding on 2018/11/7
 */
public class LogAspectRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        registryLogAspect(metadata, registry);
    }

    /**
     * 注册项目是否开启了@EnableLogAspect功能
     *
     * @param metadata metadata参数
     * @param registry Bean 定义注册器
     */
    private void registryLogAspect(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        boolean exist = metadata.hasAnnotation(EnableLogAspect.class.getName());
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(LogAspectConfiguration.class);
        if (exist) {
            // 拦截器默认不开启，只有开启了相关功能才注入到IOC，使之生效
            BeanDefinitionBuilder requestContextDefinition = BeanDefinitionBuilder
                    .genericBeanDefinition(AccessLogAspect.class);
			SpringSupport.registerIfAbsent(registry, AccessLogAspect.BEAN_NAME, requestContextDefinition);
            Map<String, Object> defaultAttrs = metadata.getAnnotationAttributes(EnableLogAspect.class.getName(), true);
            if (defaultAttrs != null && !defaultAttrs.isEmpty()) {
                defaultAttrs.forEach(builder::addPropertyValue);
            }
        }
		SpringSupport.registerIfAbsent(registry, LogAspectConfiguration.BEAN_NAME, builder);
    }
}
