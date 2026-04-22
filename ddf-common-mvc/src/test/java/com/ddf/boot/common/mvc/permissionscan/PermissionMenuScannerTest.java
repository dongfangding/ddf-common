package com.ddf.boot.common.mvc.permissionscan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

/**
 * PermissionMenuScanner 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class PermissionMenuScannerTest {

    @Test
    @DisplayName("应扫描控制器并构建菜单与按钮树")
    void shouldScanControllerAndBuildPermissionTree() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        Map<String, Object> controllerBeans = new LinkedHashMap<>();
        controllerBeans.put("demoPermissionController", new DemoPermissionController());

        when(applicationContext.getBeansWithAnnotation(Controller.class)).thenReturn(controllerBeans);
        when(applicationContext.getBeansWithAnnotation(org.springframework.web.bind.annotation.RestController.class))
            .thenReturn(new LinkedHashMap<>());
        when(applicationContext.getBean(PermissionValueSelector.class)).thenReturn(new DemoPermissionValueSelector());

        PermissionMenuScanner scanner = new PermissionMenuScanner(applicationContext);

        ScanPermissionPayload payload = scanner.scanPreAuthorizeMethods();

        assertNotNull(payload);
        assertNotNull(payload.getMenuFunctions());
        assertEquals(1, payload.getMenuFunctions().size());

        SysMenuFunction root = payload.getMenuFunctions().get(0);
        assertEquals("system", root.getCode());
        assertEquals(PermissionMenuType.CATELOG, root.getType());
        assertEquals(1, root.getChildren().size());

        SysMenuFunction menu = root.getChildren().get(0);
        assertEquals("user-menu", menu.getCode());
        assertEquals(PermissionMenuType.MENU, menu.getType());
        assertEquals(2, menu.getChildren().size());

        List<String> childCodes = menu.getChildren().stream().map(SysMenuFunction::getCode).toList();
        assertTrue(childCodes.contains("user-query"));
        assertTrue(childCodes.contains("user-create"));

        SysMenuFunction createButton = menu.getChildren()
            .stream()
            .filter(item -> "user-create".equals(item.getCode()))
            .findFirst()
            .orElseThrow();
        assertEquals(PermissionMenuType.BUTTON, createButton.getType());
        assertEquals("permission:createUser", createButton.getPermission());
    }

    @Controller
    @PermissionMenu(
        name = "用户菜单",
        code = "user-menu",
        parentName = "系统管理",
        parentCode = "system",
        parentType = PermissionMenuType.CATELOG
    )
    static class DemoPermissionController {

        @PermissionFunction(name = "查询用户", code = "user-query", permission = "query-user")
        public void queryUser() {
        }

        @PermissionFunction(name = "创建用户", code = "user-create", permission = "create-user")
        public void createUser() {
        }
    }

    static class DemoPermissionValueSelector implements PermissionValueSelector {

        @Override
        public String getPermission(Method method) {
            return "permission:" + method.getName();
        }
    }
}
