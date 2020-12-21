package com.ddf.boot.common.core.logaccess;

import java.util.Map;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 注册日志记录相关bean
 * <p>
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
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
     * @param metadata
     * @param registry
     */
    private void registryLogAspect(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        boolean exist = metadata.hasAnnotation(EnableLogAspect.class.getName());
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(LogAspectConfiguration.class);
        if (exist) {
            // 拦截器默认不开启，只有开启了相关功能才注入到IOC，使之生效
            if (!registry.containsBeanDefinition(AccessLogAspect.BEAN_NAME)) {
                BeanDefinitionBuilder requestContextDefinition = BeanDefinitionBuilder.
                        genericBeanDefinition(AccessLogAspect.class);
                registry.registerBeanDefinition(AccessLogAspect.BEAN_NAME,
                        requestContextDefinition.getBeanDefinition()
                );
            }
            Map<String, Object> defaultAttrs = metadata.getAnnotationAttributes(EnableLogAspect.class.getName(), true);
            if (defaultAttrs != null && !defaultAttrs.isEmpty()) {
                defaultAttrs.forEach(builder::addPropertyValue);
            }
        }
        registry.registerBeanDefinition(LogAspectConfiguration.BEAN_NAME, builder.getBeanDefinition());
    }
}
