package com.ddf.boot.common.mvc.permissionscan;

import com.ddf.boot.common.core.util.TreeConvertUtil;
import com.google.common.collect.Sets;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
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
		Map<String, SysMenuFunction> menus = new LinkedHashMap<>();
		PermissionValueSelector permissionValueSelector = null;
		try {
			permissionValueSelector = applicationContext.getBean(PermissionValueSelector.class);
		} catch (Exception ignore) {

		}
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
			String classPermissionMenuCode = StringUtils.defaultIfBlank(
					classPermissionMenu.code(), classPermissionMenu.name());
			if (StringUtils.isNotBlank(classPermissionMenuCode) && !menus.containsKey(classPermissionMenuCode)) {
				currentMenu.setParentCode(
						StringUtils.defaultIfBlank(classPermissionMenu.parentCode(), classPermissionMenu.parentName()));
				currentMenu.setParentName(classPermissionMenu.parentName());
				currentMenu.setType(classPermissionMenu.type());
				currentMenu.setName(classPermissionMenu.name());
				currentMenu.setCode(classPermissionMenu.code());
				currentMenu.setCode(StringUtils.defaultIfBlank(classPermissionMenu.code(), classPermissionMenu.name()));
				currentMenu.setSort(classPermissionMenu.sort());
				currentMenu.setPermission(classPermissionMenu.permission());
				currentMenu.setComponentName(
						StringUtils.defaultIfBlank(classPermissionMenu.componentName(), currentMenu.getName()));
				currentMenu.setComponentPath(
						StringUtils.defaultIfBlank(classPermissionMenu.componentPath(), currentMenu.getName()));
				menus.put(classPermissionMenuCode, currentMenu);
			}
			String classPermissionMenuParentCode = StringUtils.defaultIfBlank(
					classPermissionMenu.parentCode(), classPermissionMenu.parentName());
			if (StringUtils.isNotBlank(classPermissionMenuParentCode) && !menus.containsKey(
					classPermissionMenuParentCode)) {
				final SysMenuFunction parentMenu = new SysMenuFunction();
				// 类上的父类菜单，暂时属性太少，没有办法处理父类的父类，直接默认一级菜单了
				parentMenu.setParentCode("");
				parentMenu.setParentName("");
				parentMenu.setType(classPermissionMenu.parentType());
				parentMenu.setName(classPermissionMenu.parentName());
				parentMenu.setCode(classPermissionMenuParentCode);
				parentMenu.setSort(1);
				parentMenu.setPermission(classPermissionMenu.permission());
				parentMenu.setComponentName(
						StringUtils.defaultIfBlank(classPermissionMenu.componentName(), parentMenu.getName()));
				parentMenu.setComponentPath(
						StringUtils.defaultIfBlank(classPermissionMenu.componentPath(), parentMenu.getName()));
				menus.put(classPermissionMenuParentCode, parentMenu);
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
					if (StringUtils.isNotBlank(parentCode) && !menus.containsKey(parentCode)) {
						final SysMenuFunction parentMenu = new SysMenuFunction();
						// 暂时属性太少，一直嵌套的父级没办法支持
						parentMenu.setParentCode(StringUtils.defaultIfBlank(
								currentPermissionMenu.parentCode(),
								currentPermissionMenu.parentName()
						));
						parentMenu.setParentName(currentPermissionMenu.parentName());
						parentMenu.setType(currentPermissionMenu.type());
						parentMenu.setName(parentName);
						parentMenu.setCode(parentCode);
						parentMenu.setSort(currentPermissionMenu.sort());
						parentMenu.setPermission(currentPermissionMenu.permission());
						parentMenu.setComponentName(StringUtils.defaultIfBlank(
								currentPermissionMenu.componentName(),
								parentMenu.getName()
						));
						parentMenu.setComponentPath(StringUtils.defaultIfBlank(
								currentPermissionMenu.componentPath(),
								parentMenu.getName()
						));
						menus.put(parentCode, parentMenu);
					}
					// 处理当前PermissionFunction的父类的父类
					String grandParentCode = StringUtils.defaultIfBlank(
							currentPermissionMenu.parentCode(),
							currentPermissionMenu.parentName()
					);
					if (StringUtils.isNotBlank(grandParentCode) && !menus.containsKey(grandParentCode)) {
						final SysMenuFunction parentMenu = new SysMenuFunction();
						// 暂时属性太少，一直嵌套的父级没办法支持
						parentMenu.setParentCode("");
						parentMenu.setParentName("");
						parentMenu.setType(currentPermissionMenu.parentType());
						parentMenu.setName(currentPermissionMenu.parentName());
						parentMenu.setCode(grandParentCode);
						parentMenu.setSort(1);
						parentMenu.setPermission(currentPermissionMenu.permission());
						parentMenu.setComponentName(StringUtils.defaultIfBlank(
								currentPermissionMenu.componentName(),
								parentMenu.getName()
						));
						parentMenu.setComponentPath(StringUtils.defaultIfBlank(
								currentPermissionMenu.componentPath(),
								parentMenu.getName()
						));
						menus.put(grandParentCode, parentMenu);
					}

					final String functionCode = StringUtils.defaultIfBlank(
							permissionFunction.code(), permissionFunction.name());
					if (StringUtils.isNotBlank(functionCode) && !menus.containsKey(functionCode)) {
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
						menus.put(function.getCode(), function);
					}
				}
			}
		}
		final ArrayList<SysMenuFunction> menuList = new ArrayList<>(menus.values());
		final List<SysMenuFunction> menuTreeList = TreeConvertUtil.convert(menuList);
		final ScanPermissionPayload payload = new ScanPermissionPayload();
		payload.setMenuFunctions(menuTreeList);
		return payload;
	}
}
