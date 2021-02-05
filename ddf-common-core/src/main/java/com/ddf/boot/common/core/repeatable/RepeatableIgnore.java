package com.ddf.boot.common.core.repeatable;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>接口防重校验忽略， 忽略存在的原因是校验注解可以放在类上， 如果只有个别忽略， 可以单独标记</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/02/05 11:35
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatableIgnore {

}
