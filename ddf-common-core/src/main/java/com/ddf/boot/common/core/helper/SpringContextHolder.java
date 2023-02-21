package com.ddf.boot.common.core.helper;

import cn.hutool.extra.spring.EnableSpringUtil;
import cn.hutool.extra.spring.SpringUtil;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;

/**
 *
 *
 * 一个工具类，用于获取{@code Spring}的{@link ApplicationContext}，并直接对外暴露一些常用的的方法，
 *
 * @author dongfang.ding on 2018/12/31
 */
@Configuration
@EnableSpringUtil
@Order(value = Ordered.HIGHEST_PRECEDENCE + 5)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SpringContextHolder {

    /**
     * 下面的判空就是这个工具类存在的意义， 虽然在spring环境中这里一定不为空，但是我可能有一些工具类，
     * 需要获取系统配置类然后使用这个类获取， 但是我又希望这个类，可以提供在spring环境中的调试，
     * 如果满足这样的话， 属性初始化就必须写在静态代码块中，而这样的话工具类中使用这个类的时候applicationContext就会为空，然后空指针影响调试
     * 可参考该类 com.ddf.boot.common.websocket.util.WsSecureUtil
     *
     * @param requiredType
     * @param <T>
     * @return
     * @throws BeansException
     */
    public static <T> T getBeanWithStatic(Class<T> requiredType) throws BeansException {
        try {
            return getBean(requiredType);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T getBean(Class<T> requiredType) throws BeansException {
        return SpringUtil.getBean(requiredType);
    }

    public static Object getBean(String name) throws BeansException {
        return SpringUtil.getBean(name);
    }

    public static <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return SpringUtil.getBean(name, requiredType);
    }

    public static <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException {
        return SpringUtil.getBeansOfType(type);
    }

    /**
     * 获取{@link ApplicationContext}
     *
     * @return {@link ApplicationContext}
     */
    public static ApplicationContext getApplicationContext() {
        return SpringUtil.getApplicationContext();
    }

    /**
     * 判断某个bean类型是否存在
     *
     * @param type
     * @param <T>
     * @return
     */
    public static <T> boolean containsBeanType(Class<T> type) {
        try {
            if (Objects.isNull(SpringUtil.getBean(type))) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        } catch (NoSuchBeanDefinitionException e) {
            return Boolean.FALSE;
        }
    }
}
