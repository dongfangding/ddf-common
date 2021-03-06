package com.ddf.boot.common.jwt.config;

import com.ddf.boot.common.jwt.filter.JwtAuthorizationTokenFilter;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * jwt拦截器的导入类
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
 * @author dongfang.ding
 * @date 2019-12-07 16:45
 */
public class JwtFilterRegistrar implements ImportBeanDefinitionRegistrar {

    /**
     * 为了防止依赖包的引用，将不需要使用jwt的项目也注册了拦截器，因此需要使用方手动指定开启才导入拦截器类
     *
     * @param importingClassMetadata annotation metadata of the importing class
     * @param registry               current bean definition registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        boolean exist = importingClassMetadata.hasAnnotation(EnableJwt.class.getName());

        if (!exist) {
            return;
        }
        if (!registry.containsBeanDefinition(JwtAuthorizationTokenFilter.BEAN_NAME)) {
            BeanDefinitionBuilder requestContextDefinition = BeanDefinitionBuilder.
                    genericBeanDefinition(JwtAuthorizationTokenFilter.class);
            registry.registerBeanDefinition(JwtAuthorizationTokenFilter.BEAN_NAME,
                    requestContextDefinition.getBeanDefinition()
            );
        }
    }
}
