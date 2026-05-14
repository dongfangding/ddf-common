package com.ddf.boot.common.mvc.permissionscan;

import java.lang.reflect.Method;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2026/02/26 17:27
 */
public interface PermissionValueSelector {

    /**
     * 获取权限标识， 主要是admin模块实现方式不同，则使用的注解权限校验也不同。
     * 这里将方法暴露出去，让使用方自己解析出权限标识
     *
     * @param method method参数
     */
    String getPermission(Method method);
}
