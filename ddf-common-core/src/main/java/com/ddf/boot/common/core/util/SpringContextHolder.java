package com.ddf.boot.common.core.util;

import java.util.Map;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 一个工具类，用于获取{@code Spring}的{@link ApplicationContext}，并直接对外暴露一些常用的的方法，
 * 如果需要这里未提供的方法，可以调用{@link SpringContextHolder#getApplicationContext()}然后再具体操作它的方法
 * <p>
 * <p>
 * <p>
 * <p>
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
 * @author dongfang.ding on 2018/12/31
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    @Getter
    private static ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.applicationContext = applicationContext;
    }

    // 下面的判空就是这个工具类存在的意义， 虽然在spring环境中这里一定不为空，但是我可能有一些工具类，
    // 需要获取系统配置类然后使用这个类获取， 但是我又希望这个类，可以提供在spring环境中的调试，
    // 如果满足这样的话， 属性初始化就必须写在静态代码块中，而这样的话工具类中使用这个类的时候applicationContext就会为空，然后空指针影响调试
    // 可参考该类 com.ddf.boot.common.websocket.util.WsSecureUtil
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
}
