package com.ddf.boot.common.mvc.permissionscan;

import com.ddf.boot.common.core.util.TreeConvertUtil;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

public class PermissionMenuScanner {
    private final ApplicationContext applicationContext;

    public PermissionMenuScanner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ScanPermissionPayload scanPreAuthorizeMethods() {
        Map<String, SysMenuFunction> menus = scanControllers();
        final List<SysMenuFunction> menuTreeList = TreeConvertUtil.convert(new ArrayList<>(menus.values()));
        final ScanPermissionPayload payload = new ScanPermissionPayload();
        payload.setMenuFunctions(menuTreeList);
        return payload;
    }

    private Map<String, SysMenuFunction> scanControllers() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Controller.class);
        beans.putAll(applicationContext.getBeansWithAnnotation(RestController.class));
        final PermissionValueSelector permissionValueSelector = resolvePermissionValueSelector();
        final Map<String, SysMenuFunction> menus = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            final Class<?> targetClass = AopUtils.getTargetClass(entry.getValue());
            final PermissionMenu classPermissionMenu = targetClass.getAnnotation(PermissionMenu.class);
            // 为避免扫描类过多，必须类上加这个注解
            // 如果一个类中有多个菜单下的接口权限，那么就会在每个方法上标注当前接口所属的父类，类上的注解就变的没有意义，此时可以定义一个空注解，不能省略
            // 例如：@PermissionMenu(name = "", parentName = "")
            if (classPermissionMenu == null) {
                continue;
            }
            scanClassMenu(menus, classPermissionMenu);
            scanMethods(menus, targetClass, classPermissionMenu, permissionValueSelector);
        }
        return menus;
    }

    private PermissionValueSelector resolvePermissionValueSelector() {
        try {
            return applicationContext.getBean(PermissionValueSelector.class);
        } catch (Exception ignore) {
            return null;
        }
    }

    private void scanClassMenu(Map<String, SysMenuFunction> menus, PermissionMenu classPermissionMenu) {
        final String classPermissionMenuCode = StringUtils.defaultIfBlank(classPermissionMenu.code(),
                classPermissionMenu.name());
        if (StringUtils.isNotBlank(classPermissionMenuCode)) {
            putMenuIfAbsent(menus, buildMenu(
                    StringUtils.defaultIfBlank(classPermissionMenu.parentCode(), classPermissionMenu.parentName()),
                    classPermissionMenu.parentName(), classPermissionMenu.type(), classPermissionMenu.name(),
                    classPermissionMenuCode, classPermissionMenu.sort(), classPermissionMenu.permission(),
                    classPermissionMenu.componentName(), classPermissionMenu.componentPath()));
        }
        final String classPermissionMenuParentCode = StringUtils.defaultIfBlank(classPermissionMenu.parentCode(),
                classPermissionMenu.parentName());
        if (StringUtils.isNotBlank(classPermissionMenuParentCode)) {
            // 类上的父类菜单，暂时属性太少，没有办法处理父类的父类，直接默认一级菜单了
            putMenuIfAbsent(menus, buildMenu("", "", classPermissionMenu.parentType(), classPermissionMenu.parentName(),
                    classPermissionMenuParentCode, 1, classPermissionMenu.permission(),
                    classPermissionMenu.componentName(), classPermissionMenu.componentPath()));
        }
    }

    private void scanMethods(Map<String, SysMenuFunction> menus, Class<?> targetClass,
            PermissionMenu classPermissionMenu, PermissionValueSelector permissionValueSelector) {
        for (Method method : targetClass.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(PermissionFunction.class)) {
                continue;
            }
            final PermissionFunction permissionFunction = method.getAnnotation(PermissionFunction.class);
            PermissionMenu currentPermissionMenu = classPermissionMenu;
            if (StringUtils.isNotBlank(permissionFunction.menu().name())) {
                currentPermissionMenu = permissionFunction.menu();
            }
            final String parentName = currentPermissionMenu.name();
            final String parentCode = StringUtils.defaultIfBlank(currentPermissionMenu.code(), parentName);

            scanParentMenu(menus, currentPermissionMenu, parentName, parentCode);
            scanGrandParentMenu(menus, currentPermissionMenu);
            scanFunction(menus, method, permissionFunction, permissionValueSelector, parentName, parentCode);
        }
    }

    /**
     * 处理当前PermissionFunction的父类菜单
     */
    private void scanParentMenu(Map<String, SysMenuFunction> menus, PermissionMenu currentPermissionMenu,
            String parentName, String parentCode) {
        if (StringUtils.isBlank(parentCode)) {
            return;
        }
        // 暂时属性太少，一直嵌套的父级没办法支持
        putMenuIfAbsent(menus, buildMenu(
                StringUtils.defaultIfBlank(currentPermissionMenu.parentCode(), currentPermissionMenu.parentName()),
                currentPermissionMenu.parentName(), currentPermissionMenu.type(), parentName, parentCode,
                currentPermissionMenu.sort(), currentPermissionMenu.permission(), currentPermissionMenu.componentName(),
                currentPermissionMenu.componentPath()));
    }

    /**
     * 处理当前PermissionFunction的父类的父类
     */
    private void scanGrandParentMenu(Map<String, SysMenuFunction> menus, PermissionMenu currentPermissionMenu) {
        final String grandParentCode = StringUtils.defaultIfBlank(currentPermissionMenu.parentCode(),
                currentPermissionMenu.parentName());
        if (StringUtils.isBlank(grandParentCode)) {
            return;
        }
        // 暂时属性太少，一直嵌套的父级没办法支持
        putMenuIfAbsent(menus, buildMenu("", "", currentPermissionMenu.parentType(), currentPermissionMenu.parentName(),
                grandParentCode, 1, currentPermissionMenu.permission(), currentPermissionMenu.componentName(),
                currentPermissionMenu.componentPath()));
    }

    private void scanFunction(Map<String, SysMenuFunction> menus, Method method, PermissionFunction permissionFunction,
            PermissionValueSelector permissionValueSelector, String parentName, String parentCode) {
        final String functionCode = StringUtils.defaultIfBlank(permissionFunction.code(), permissionFunction.name());
        if (StringUtils.isBlank(functionCode)) {
            return;
        }
        final SysMenuFunction function = new SysMenuFunction();
        function.setParentCode(parentCode);
        function.setParentName(parentName);
        function.setType(PermissionMenuType.BUTTON);
        function.setCode(functionCode);
        function.setName(permissionFunction.name());
        function.setSort(permissionFunction.sort());
        if (Objects.nonNull(permissionValueSelector)) {
            function.setPermission(permissionValueSelector.getPermission(method));
        } else {
            function.setPermission(permissionFunction.permission());
        }
        putMenuIfAbsent(menus, function);
    }

    /**
     * 构建菜单节点，组件名称/路径缺省时回退为菜单名称。
     */
    private SysMenuFunction buildMenu(String parentCode, String parentName, PermissionMenuType type, String name,
            String code, int sort, String permission, String componentName, String componentPath) {
        final SysMenuFunction menu = new SysMenuFunction();
        menu.setParentCode(parentCode);
        menu.setParentName(parentName);
        menu.setType(type);
        menu.setName(name);
        menu.setCode(code);
        menu.setSort(sort);
        menu.setPermission(permission);
        menu.setComponentName(StringUtils.defaultIfBlank(componentName, name));
        menu.setComponentPath(StringUtils.defaultIfBlank(componentPath, name));
        return menu;
    }

    /**
     * 仅在 code 非空且尚未存在时写入菜单。
     */
    private void putMenuIfAbsent(Map<String, SysMenuFunction> menus, SysMenuFunction menu) {
        if (StringUtils.isNotBlank(menu.getCode()) && !menus.containsKey(menu.getCode())) {
            menus.put(menu.getCode(), menu);
        }
    }
}
