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
 * @date 2025/04/28 17:29
 */
@Inherited
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionMenu {

    /**
     * 菜单标题
     *
     * @return
     */
    String name();

    /**
     * 菜单编码，如果需要的话，不需要默认取菜单标题
     *
     * @return
     */
    String code() default "";

    /**
     * 菜单类型
     *
     * @return
     */
    PermissionMenuType type() default PermissionMenuType.MENU;

    /**
     * 菜单权限
     *
     * @return
     */
    String permission() default "";

    /**
     * 父类菜单名称
     *
     * @return
     */
    String parentName() default "";


    /**
     * 父类菜单编码，如果需要的话，不需要默认取父类菜单名称
     *
     * @return
     */
    String parentCode() default "";

    /**
     * 菜单类型
     *
     * @return
     */
    PermissionMenuType parentType() default PermissionMenuType.FOLDER;

}
