package com.ddf.boot.common.authentication.config;

import com.ddf.boot.common.api.support.SpringSupport;
import com.ddf.boot.common.authentication.annotation.EnableAuthenticate;
import com.ddf.boot.common.authentication.filter.AuthenticateTokenFilter;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 认证模块bean注册类
 *
 * @author dongfang.ding
 * @since 2019-12-07 16:45
 */
public class AuthenticateFilterRegistrar implements ImportBeanDefinitionRegistrar {


    /**
     * 为了防止依赖包的引用，将不需要使用认证的项目也注册了拦截器，因此需要使用方手动指定开启才导入拦截器类
     *
     * @param importingClassMetadata importing类型metadata参数
     * @param registry               Bean 定义注册器
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        boolean exist = importingClassMetadata.hasAnnotation(EnableAuthenticate.class.getName());

        if (!exist) {
            return;
        }
        BeanDefinitionBuilder requestContextDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(AuthenticateTokenFilter.class);
		SpringSupport.registerIfAbsent(registry, AuthenticateTokenFilter.BEAN_NAME, requestContextDefinition);
    }
}
