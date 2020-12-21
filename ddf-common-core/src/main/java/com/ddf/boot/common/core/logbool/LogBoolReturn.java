package com.ddf.boot.common.core.logbool;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>记录boolean修改的结果</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/06/12 15:12
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogBoolReturn {

    /**
     * 日志名称
     *
     * @return
     */
    String logName() default "";
}
