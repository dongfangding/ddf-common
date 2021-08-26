package com.ddf.common.ons.consumer;

import com.aliyun.openservices.ons.api.Message;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ONS消息监听器接口
 *
 * @author snowball
 * @date 2021/8/26 16:29
 **/
public interface OnsMessageListener<D extends Serializable> {

    Logger LOGGER = LoggerFactory.getLogger("OnsConsumer");
    Logger ERROR_LOGGER = LoggerFactory.getLogger("OnsConsumerError");

    /**
     * 默认消费幂等性过期时间1天
     */
    long DEFAULT_IDEMPOTENT_EXPIRE_SECONDS = 86400L;

    /**
     * 解析泛型领域类
     *
     * @return
     */
    default Class<D> parseDomainClass() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
        return (Class) actualTypeArguments[0];
    }

    /**
     * 获取领域对象类
     *
     * @return
     */
    Class<D> getDomainClass();

    /**
     * 获取保证幂等性的成员
     * 必须保证同一个Topic下的唯一
     *
     * @return
     */
    default String getMember() {
        return this.getClass().getCanonicalName();
    }

    /**
     * 获取幂等性的过期时间，单位秒
     * 默认三天
     *
     * @return
     */
    default long getIdempotentExpireSeconds() {
        return DEFAULT_IDEMPOTENT_EXPIRE_SECONDS;
    }

    /**
     * 获取消息监听器注解
     *
     * @return
     */
    default com.ddf.common.ons.annotation.OnsMessageListener getAnnotation() {
        return this.getClass()
                .getAnnotation(com.ddf.common.ons.annotation.OnsMessageListener.class);
    }

    /**
     * 业务是否执行成功
     *
     * @param message
     * @param domain
     * @return
     * @since 1.3.0
     */
    boolean isBizSuccess(Message message, D domain);

    /**
     * 执行业务方法
     *
     * @param domain 业务领域累
     */
    void executeBiz(D domain) throws ConsumeException;

}

