package com.ddf.boot.common.mvc.permissionscan;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>标识一个功能权限</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2025/04/28 17:29
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionFunction {

    /**
     * 接口功能名称
     *
     * @return
     */
    String name();

    /**
     * 接口功能代码
     *
     * @return
     */
    String code() default "";

    /**
     * 权限代码
     *
     * @return
     */
    String permission() default "";

    /**
     * 所属菜单，如果没有指定，则使用类上的{@link PermissionMenu}, 这个的一大作用是一个controller里包含了两个菜单的功能，就没有办法把父类菜单标记到类上了，需要方法指定自己的父类菜单
     *
     * @return
     */
    PermissionMenu menu() default @PermissionMenu(name = "");

    /**
     * 排序
     *
     * @return
     */
    int sort() default 1;

}
