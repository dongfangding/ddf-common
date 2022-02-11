package com.ddf.boot.common.core.requestsign;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>请求验签</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/12/01 14:07
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestSign {

    /**
     * 是否进行重新判断
     *
     * @return
     */
    boolean nonce() default false;

    /**
     * 视作重新攻击的时间间隔，单位秒
     *
     * @return
     */
    long nonceIntervalSeconds() default 60;
}
