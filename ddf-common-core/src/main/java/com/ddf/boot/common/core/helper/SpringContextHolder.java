package com.ddf.boot.common.core.helper;

import java.util.Map;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 注意，无法在容器初始化过程中工作。因为无法保证顺序， 容器初始化的时候，这个类可能还没初始化，会有空指针异常，除非强制依赖该类。
 *
 *
 * 一个工具类，用于获取{@code Spring}的{@link ApplicationContext}，并直接对外暴露一些常用的的方法，
 * 如果需要这里未提供的方法，可以调用{@link SpringContextHolder#getApplicationContext()}然后再具体操作它的方法
 *
 * @author dongfang.ding on 2018/12/31
 */
@Component
@Order(value = Ordered.HIGHEST_PRECEDENCE + 5)
public class SpringContextHolder implements ApplicationContextAware {

    @Getter
    private static ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.applicationContext = applicationContext;
    }


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
        return applicationContext == null ? null : applicationContext.getBean(requiredType);
    }

    public static <T> T getBean(Class<T> requiredType) throws BeansException {
        return applicationContext.getBean(requiredType);
    }

    public static Object getBean(String name) throws BeansException {
        return applicationContext.getBean(name);
    }

    public static <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return applicationContext.getBean(name, requiredType);
    }

    public static <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException {
        return applicationContext.getBeansOfType(type);
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
            applicationContext.getBean(type);
            return Boolean.TRUE;
        } catch (NoSuchBeanDefinitionException e) {
            return Boolean.FALSE;
        }
    }
}
