package com.ddf.boot.common.mvc.permissionscan;

import com.ddf.boot.common.core.util.TreeConvertUtil;
import com.google.common.collect.Sets;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PermissionMenuScanner {

    private final ApplicationContext applicationContext;

    public PermissionMenuScanner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    // 预编译正则
    Pattern pattern = Pattern.compile("@el\\.check\\('([^']+)'\\)");


    public ScanPermissionPayload scanPreAuthorizeMethods() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(
                org.springframework.stereotype.Controller.class);
        beans.putAll(applicationContext.getBeansWithAnnotation(
                org.springframework.web.bind.annotation.RestController.class));
        Set<SysMenuFunction> menus = Sets.newHashSet();
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object bean = entry.getValue();
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            PermissionMenu classPermissionMenu = targetClass.getAnnotation(PermissionMenu.class);
            // 为避免扫描类过多，必须类上加这个注解
            // 如果一个类中有多个菜单下的接口权限，那么就会在每个方法上标注当前接口所属的父类，类上的注解就变的没有意义，此时可以定义一个空注解，不能省略
            // 例如：@PermissionMenu(name = "", parentName = "")
            if (classPermissionMenu == null) {
                continue;
            }
            final SysMenuFunction currentMenu = new SysMenuFunction();
            if (StringUtils.isNotBlank(classPermissionMenu.name())) {
                currentMenu.setParentCode(
                        StringUtils.defaultIfBlank(classPermissionMenu.parentCode(), classPermissionMenu.parentName()));
                currentMenu.setParentName(classPermissionMenu.parentName());
                currentMenu.setType(classPermissionMenu.type());
                currentMenu.setName(classPermissionMenu.name());
                currentMenu.setCode(StringUtils.defaultIfBlank(classPermissionMenu.code(), classPermissionMenu.name()));
                currentMenu.setSort(1);
                currentMenu.setPermission(classPermissionMenu.permission());
                menus.add(currentMenu);
            }
            if (StringUtils.isNotBlank(classPermissionMenu.parentCode()) || StringUtils.isNotBlank(
                    classPermissionMenu.parentName())) {
                final SysMenuFunction parentMenu = new SysMenuFunction();
                // 类上的父类菜单，暂时属性太少，没有办法处理父类的父类，直接默认一级菜单了
                parentMenu.setParentCode("");
                parentMenu.setParentName("");
                parentMenu.setType(classPermissionMenu.parentType());
                parentMenu.setName(classPermissionMenu.parentName());
                parentMenu.setCode(
                        StringUtils.defaultIfBlank(classPermissionMenu.parentCode(), classPermissionMenu.parentName()));
                parentMenu.setSort(1);
                parentMenu.setPermission(classPermissionMenu.permission());
                menus.add(parentMenu);
            }

            for (Method method : targetClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(PermissionFunction.class)) {
                    PermissionFunction permissionFunction = method.getAnnotation(PermissionFunction.class);
                    PermissionMenu currentPermissionMenu = classPermissionMenu;
                    if (StringUtils.isNotBlank(permissionFunction
                            .menu()
                            .name())) {
                        currentPermissionMenu = permissionFunction.menu();
                    }
                    String parentName = currentPermissionMenu.name();
                    String parentCode = StringUtils.defaultIfBlank(currentPermissionMenu.code(), parentName);

                    // 处理当前PermissionFunction的父类菜单
                    if (StringUtils.isNotBlank(parentName) || StringUtils.isNotBlank(parentCode)) {
                        final SysMenuFunction parentMenu = new SysMenuFunction();
                        // 暂时属性太少，一直嵌套的父级没办法支持
                        parentMenu.setParentCode(StringUtils.defaultIfBlank(currentPermissionMenu.parentCode(),
                                currentPermissionMenu.parentName()
                        ));
                        parentMenu.setParentName(currentPermissionMenu.parentName());
                        parentMenu.setType(currentPermissionMenu.type());
                        parentMenu.setName(parentName);
                        parentMenu.setCode(parentCode);
                        parentMenu.setSort(1);
                        parentMenu.setPermission(currentPermissionMenu.permission());
                        menus.add(parentMenu);
                    }
                    // 处理当前PermissionFunction的父类的父类
                    if (StringUtils.isNotBlank(currentPermissionMenu.parentCode()) || StringUtils.isNotBlank(
                            currentPermissionMenu.parentName())) {
                        final SysMenuFunction parentMenu = new SysMenuFunction();
                        // 暂时属性太少，一直嵌套的父级没办法支持
                        parentMenu.setParentCode("");
                        parentMenu.setParentName("");
                        parentMenu.setType(currentPermissionMenu.parentType());
                        parentMenu.setName(currentPermissionMenu.parentName());
                        parentMenu.setCode(StringUtils.defaultIfBlank(currentPermissionMenu.parentCode(),
                                currentPermissionMenu.parentName()
                        ));
                        parentMenu.setSort(1);
                        parentMenu.setPermission(currentPermissionMenu.permission());
                        menus.add(parentMenu);
                    }

                    final SysMenuFunction function = new SysMenuFunction();
                    function.setParentCode(parentCode);
                    function.setParentName(parentName);
                    function.setType(PermissionMenuType.BUTTON);
                    function.setCode(StringUtils.defaultIfBlank(permissionFunction.code(), permissionFunction.name()));
                    function.setName(permissionFunction.name());
                    function.setSort(1);
                    function.setPermission(permissionFunction.permission());
                    menus.add(function);
                }
            }
        }
        final ArrayList<SysMenuFunction> menuList = new ArrayList<>(menus);
        final List<SysMenuFunction> menuTreeList = TreeConvertUtil.convert(menuList);
        final ScanPermissionPayload payload = new ScanPermissionPayload();
        payload.setMenuFunctions(menuTreeList);
        return payload;
    }

//    public void autoCreateMenu(ScanPermissionPayload payload) {
//        final List<SysMenuFunction> functions = payload.getMenuFunctions();
//        if (CollectionUtils.isEmpty(functions)) {
//            log.info("未扫描到菜单信息，无需自动创建菜单");
//            return;
//        }
//
//        Map<String, Menu> menuCacheMap = new HashMap<>();
//        List<Menu> saveList = new ArrayList<>();
//        Set<Long> updatedParentIds = new HashSet<>();
//        for (SysMenuFunction function : functions) {
//            recursiveCreateMenu(function, null, menuCacheMap, saveList, updatedParentIds);
//        }
//        saveBatch(saveList);
//        for (Long parentId : updatedParentIds) {
//            updateSubCnt(parentId);
//        }
//    }
//
//    private void recursiveCreateMenu(SysMenuFunction function, Menu parentMenu, Map<String, Menu> menuCacheMap,
//            List<Menu> saveList, Set<Long> updatedParentIds) {
//        // 判断当前节点是否已存在
//        Menu existing = menuMapper.findByTitle(function.getCode());
//        Menu currentMenu;
//        if (existing != null) {
//            currentMenu = existing;
//            // 可选：更新权限或其他属性
//            // if (!Objects.equals(existing.getPermission(), function.getPermission())) {
//            //     existing.setPermission(function.getPermission());
//            //     update(existing);
//            // }
//
//        } else {
//            currentMenu = new Menu();
//            currentMenu.setTitle(function.getCode());
//            currentMenu.setComponentName(function.getCode());
//            currentMenu.setMenuSort(function.getSort());
//            currentMenu.setComponent(function.getCode());
//            currentMenu.setPath("");
//            currentMenu.setType(convertMenuType(function.getType()));
//            currentMenu.setPermission(function.getPermission());
//            currentMenu.setSubCount(0);
//            if (parentMenu != null) {
//                currentMenu.setPid(parentMenu.getId());
//            }
//            // 因为有绑定关系，这里如果不保存，后面子节点拿不到父类的id，导致绑定不上，只能每处理一次都保存了
//            //            saveList.add(currentMenu);
//            save(currentMenu);
//        }
//
//        // 缓存当前节点，方便后面查找子节点的父级
//        menuCacheMap.put(function.getCode(), currentMenu);
//
//        if (parentMenu != null) {
//            updatedParentIds.add(parentMenu.getId());
//        }
//
//        // 递归处理子节点
//        if (!CollectionUtils.isEmpty(function.getChildren())) {
//            for (SysMenuFunction child : function.getChildren()) {
//                recursiveCreateMenu(child, currentMenu, menuCacheMap, saveList, updatedParentIds);
//            }
//        }
//    }
//
//    private Integer convertMenuType(PermissionMenuType menuType) {
//        if (Objects.equals(PermissionMenuType.FOLDER, menuType)) {
//            return 0;
//        } else if (Objects.equals(PermissionMenuType.MENU, menuType)) {
//            return 1;
//        } else {
//            return 2;
//        }
//    }



    //    public ScanPermissionPayload scanPreAuthorizeMethods() {
    //        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(
    //                org.springframework.stereotype.Controller.class);
    //        beans.putAll(applicationContext.getBeansWithAnnotation(
    //                org.springframework.web.bind.annotation.RestController.class));
    //        Set<SysMenuFunction> menus = Sets.newHashSet();
    //        for (Map.Entry<String, Object> entry : beans.entrySet()) {
    //            Object bean = entry.getValue();
    //            Class<?> targetClass = AopUtils.getTargetClass(bean);
    //            PermissionMenu classPermissionMenu = targetClass.getAnnotation(PermissionMenu.class);
    //            // 为避免扫描类过多，必须类上加这个注解
    //            // 如果一个类中有多个菜单下的接口权限，那么就会在每个方法上标注当前接口所属的父类，类上的注解就变的没有意义，此时可以定义一个空注解，不能省略
    //            // 例如：@PermissionMenu(name = "", parentName = "")
    //            if (classPermissionMenu == null) {
    //                continue;
    //            }
    //            final SysMenuFunction currentMenu = new SysMenuFunction();
    //            if (StringUtils.isNotBlank(classPermissionMenu.name())) {
    //                currentMenu.setParentCode(
    //                        StringUtils.defaultIfBlank(classPermissionMenu.parentCode(), classPermissionMenu.parentName()));
    //                currentMenu.setParentName(classPermissionMenu.parentName());
    //                currentMenu.setType(classPermissionMenu.type());
    //                currentMenu.setName(classPermissionMenu.name());
    //                currentMenu.setCode(StringUtils.defaultIfBlank(classPermissionMenu.code(), classPermissionMenu.name()));
    //                currentMenu.setSort(1);
    //                currentMenu.setPermission(classPermissionMenu.permission());
    //                menus.add(currentMenu);
    //            }
    //            if (StringUtils.isNotBlank(classPermissionMenu.parentCode()) || StringUtils.isNotBlank(
    //                    classPermissionMenu.parentName())) {
    //                final SysMenuFunction parentMenu = new SysMenuFunction();
    //                // 类上的父类菜单，暂时属性太少，没有办法处理父类的父类，直接默认一级菜单了
    //                parentMenu.setParentCode("");
    //                parentMenu.setParentName("");
    //                parentMenu.setType(classPermissionMenu.parentType());
    //                parentMenu.setName(classPermissionMenu.parentName());
    //                parentMenu.setCode(
    //                        StringUtils.defaultIfBlank(classPermissionMenu.parentCode(), classPermissionMenu.parentName()));
    //                parentMenu.setSort(1);
    //                parentMenu.setPermission(classPermissionMenu.permission());
    //                menus.add(parentMenu);
    //            }
    //
    //            for (Method method : targetClass.getDeclaredMethods()) {
    //                if (method.isAnnotationPresent(PreAuthorize.class) && method.isAnnotationPresent(
    //                        PermissionFunction.class)) {
    //                    PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
    //                    PermissionFunction permissionFunction = method.getAnnotation(PermissionFunction.class);
    //                    PermissionMenu currentPermissionMenu = classPermissionMenu;
    //                    if (StringUtils.isNotBlank(permissionFunction
    //                            .menu()
    //                            .name())) {
    //                        currentPermissionMenu = permissionFunction.menu();
    //                    }
    //                    String parentName = currentPermissionMenu.name();
    //                    String parentCode = StringUtils.defaultIfBlank(currentPermissionMenu.code(), parentName);
    //
    //                    // 处理当前PermissionFunction的父类菜单
    //                    if (StringUtils.isNotBlank(parentName) || StringUtils.isNotBlank(parentCode)) {
    //                        final SysMenuFunction parentMenu = new SysMenuFunction();
    //                        // 暂时属性太少，一直嵌套的父级没办法支持
    //                        parentMenu.setParentCode(StringUtils.defaultIfBlank(currentPermissionMenu.parentCode(),
    //                                currentPermissionMenu.parentName()
    //                        ));
    //                        parentMenu.setParentName(currentPermissionMenu.parentName());
    //                        parentMenu.setType(currentPermissionMenu.type());
    //                        parentMenu.setName(parentName);
    //                        parentMenu.setCode(parentCode);
    //                        parentMenu.setSort(1);
    //                        parentMenu.setPermission(currentPermissionMenu.permission());
    //                        menus.add(parentMenu);
    //                    }
    //                    // 处理当前PermissionFunction的父类的父类
    //                    if (StringUtils.isNotBlank(currentPermissionMenu.parentCode()) || StringUtils.isNotBlank(
    //                            currentPermissionMenu.parentName())) {
    //                        final SysMenuFunction parentMenu = new SysMenuFunction();
    //                        // 暂时属性太少，一直嵌套的父级没办法支持
    //                        parentMenu.setParentCode("");
    //                        parentMenu.setParentName("");
    //                        parentMenu.setType(currentPermissionMenu.parentType());
    //                        parentMenu.setName(currentPermissionMenu.parentName());
    //                        parentMenu.setCode(StringUtils.defaultIfBlank(currentPermissionMenu.parentCode(),
    //                                currentPermissionMenu.parentName()
    //                        ));
    //                        parentMenu.setSort(1);
    //                        parentMenu.setPermission(currentPermissionMenu.permission());
    //                        menus.add(parentMenu);
    //                    }
    //
    //                    final SysMenuFunction function = new SysMenuFunction();
    //                    function.setParentCode(parentCode);
    //                    function.setParentName(parentName);
    //                    function.setType(PermissionMenuType.BUTTON);
    //                    function.setCode(StringUtils.defaultIfBlank(permissionFunction.code(), permissionFunction.name()));
    //                    function.setName(permissionFunction.name());
    //                    function.setSort(1);
    //                    Matcher matcher = pattern.matcher(preAuthorize.value());
    //                    if (matcher.find()) {
    //                        function.setPermission(matcher.group(1));
    //                    }
    //                    menus.add(function);
    //                }
    //            }
    //        }
    //        final ArrayList<SysMenuFunction> menuList = new ArrayList<>(menus);
    //        final List<SysMenuFunction> menuTreeList = TreeConvertUtil.convert(menuList);
    //        final ScanPermissionPayload payload = new ScanPermissionPayload();
    //        payload.setMenuFunctions(menuTreeList);
    //        return payload;
    //    }
}
