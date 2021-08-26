package com.ddf.boot.common.core.controllerwrapper;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>忽略包装标识的方法</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/08/26 20:19
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WrapperIgnore {
}
