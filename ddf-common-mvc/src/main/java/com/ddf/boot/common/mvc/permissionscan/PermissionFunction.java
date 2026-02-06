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
     * 权限代码，本项目中直接读取Spring security {@link PreAuthorize}中的表达式
     * 如 @PreAuthorize("@el.check('system-config:set')")
     *
     * 如果不适用的话，则使用
     *
     * @return
     */
    String permission() default "";

    /**
     * 所属菜单，如果没有指定，则使用类上的{@link PermissionMenu}
     *
     * @return
     */
    PermissionMenu menu() default @PermissionMenu(name = "");

}
