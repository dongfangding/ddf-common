package com.ddf.boot.common.mvc.resolver;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>标识参数可以被多个参数解析器尝试进行参数解析</p >
 *
 * 同一个参数支持application/json和application/x-www-form-urlencoded
 *
 * @see MultiArgumentResolverMethodProcessor
 * @author Snowball
 * @version 1.0
 * @date 2020/08/31 18:57
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MultiArgumentResolver {
}
