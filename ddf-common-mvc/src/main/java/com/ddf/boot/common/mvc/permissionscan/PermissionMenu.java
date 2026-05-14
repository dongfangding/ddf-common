package com.ddf.boot.common.mvc.permissionscan;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>标识菜单</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2025/04/28 17:29
 */
@Inherited
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionMenu {

    /**
     * 菜单标题
     */
    String name();

    /**
     * 菜单编码，如果需要的话，不需要默认取菜单标题
     */
    String code() default "";

    /**
     * 菜单类型
     */
    PermissionMenuType type() default PermissionMenuType.MENU;

    /**
     * 组件名称/路由地址，前端用，两个字段合一，保持一样了
     */
    String componentName() default "";

    /**
     * 组件路径，前端用
     */
    String componentPath() default "";

    /**
     * 菜单权限
     */
    String permission() default "";

    /**
     * 父类菜单名称
     */
    String parentName() default "";


    /**
     * 父类菜单编码，如果需要的话，不需要默认取父类菜单名称
     */
    String parentCode() default "";

    /**
     * 菜单类型
     */
    PermissionMenuType parentType() default PermissionMenuType.CATELOG;

    /**
     * 排序
     */
    int sort() default 1;

}
