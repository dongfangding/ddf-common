package com.ddf.boot.common.authentication.interfaces;

import com.ddf.boot.common.authentication.config.AuthenticationProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * <p用来外部刷新{@link AuthenticationProperties}配置类的</p >
 * 外部通过配置中心将最新属性刷新回认证配置类中，因为本模块不准备依赖配置中心.
 *
 * 继承该类并加入到容器中
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/05/28 20:04
 */
public interface AuthenticationPropertiesRefreshSupport extends InitializingBean, ApplicationContextAware {

    /**
     * 由于接口中的属性都是不可变的，只能用对象来装ApplicationContext，以供使用
     */
    Map<String, ApplicationContext> MAP = new HashMap<>();

    /**
     * 加密算法秘钥
     */
    abstract String getSecret();

    /**
     * token header name, 控制客户端通过哪个header传token
     */
    abstract String getTokenHeaderName();

    /**
     * token的value前缀, 控制token是不是包含前缀，token本身不包含，客户端传送过来的时候要包含
     */
    abstract String getTokenPrefix();

    /**
     * 用来校验token中credit身份的header name, 如果生成token的时候不放入，则不会校验
     * 如果是移动端，一般是设备号什么之类的
     */
    String getCreditHeaderName();

    /**
     * 忽略路径
     */
    List<String> getIgnores();

    /**
     * token过期时间
     * 单位  分钟
     */
    Integer getExpiredMinute();

    /**
     * 是否开启mock登录用户功能， 如果开启的话，那么则不会走token解析流程，也不走认证流程，而是把token直接作为用户id放入到上下文中直接使用,
     * 则当前请求可以直接非常简单的用对应身份进行操作
     * 需要注意，这个token 仍然需要保持原token格式， 即
     * Authorization：Bearer 1 则当前用户id为1
     */
    boolean isMock();

    /**
     * 为安全起见，必须配置在其中的mock用户才能使用
     */
    List<String> getMockUserIdList();

    /**
     * 业务相关参数
     *
     * @return
     */
    AuthenticationProperties.Biz getBiz();


    @Override
    default void setApplicationContext(ApplicationContext context) throws BeansException {
        MAP.put(this.getClass().getName(), context);
    }

    /**
     * 将接口返回值赋值给属性类
     *
     * @throws Exception
     */
    @Override
    default void afterPropertiesSet() throws Exception {
        AuthenticationProperties properties = (AuthenticationProperties) MAP.get(this.getClass().getName()).getBean(AuthenticationProperties.BEAN_NAME);
        copyToProperties(properties);
    }

    /**
     * 复制接口返回值给属性类
     *
     * @param properties
     */
    default void copyToProperties(AuthenticationProperties properties) {
        properties.setSecret(getSecret());
        properties.setTokenHeaderName(getTokenHeaderName());
        properties.setTokenPrefix(getTokenPrefix());
        properties.setCreditHeaderName(getCreditHeaderName());
        properties.setIgnores(getIgnores());
        properties.setExpiredMinute(getExpiredMinute());
        properties.setMock(isMock());
        properties.setMockUserIdList(getMockUserIdList());
        properties.setBiz(getBiz());
    }
}
