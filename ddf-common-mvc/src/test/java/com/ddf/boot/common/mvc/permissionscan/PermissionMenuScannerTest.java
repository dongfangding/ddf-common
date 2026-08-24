package com.ddf.boot.common.mvc.permissionscan;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.web.bind.annotation.RestController;

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
        when(applicationContext.getBeansWithAnnotation(
                org.springframework.web.bind.annotation.RestController.class)).thenReturn(new LinkedHashMap<>());
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

    @Test
    @DisplayName("无 @PermissionMenu 注解的 Controller 应被跳过")
    void shouldSkipControllerWithoutPermissionMenuAnnotation() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        Map<String, Object> controllerBeans = new LinkedHashMap<>();
        controllerBeans.put("plainController", new PlainController());
        when(applicationContext.getBeansWithAnnotation(Controller.class)).thenReturn(controllerBeans);
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(new LinkedHashMap<>());

        ScanPermissionPayload payload = new PermissionMenuScanner(applicationContext).scanPreAuthorizeMethods();

        assertThat(payload.getMenuFunctions()).isEmpty();
    }

    @Test
    @DisplayName("无任何 Controller Bean 时返回空菜单")
    void shouldReturnEmptyWhenNoControllerBeans() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBeansWithAnnotation(Controller.class)).thenReturn(new LinkedHashMap<>());
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(new LinkedHashMap<>());

        ScanPermissionPayload payload = new PermissionMenuScanner(applicationContext).scanPreAuthorizeMethods();

        assertThat(payload.getMenuFunctions()).isEmpty();
    }

    @Test
    @DisplayName("重复 code 的菜单应被去重（putMenuIfAbsent）")
    void shouldDeduplicateMenusWithSameCode() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        Map<String, Object> controllerBeans = new LinkedHashMap<>();
        controllerBeans.put("dupControllerOne", new DupControllerOne());
        controllerBeans.put("dupControllerTwo", new DupControllerTwo());
        when(applicationContext.getBeansWithAnnotation(Controller.class)).thenReturn(controllerBeans);
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(new LinkedHashMap<>());

        ScanPermissionPayload payload = new PermissionMenuScanner(applicationContext).scanPreAuthorizeMethods();

        assertThat(payload.getMenuFunctions()).hasSize(1);
        assertThat(payload.getMenuFunctions().get(0).getCode()).isEqualTo("dup-code");
    }

    @Test
    @DisplayName("应扫描 @RestController 注解的控制器")
    void shouldScanRestController() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBeansWithAnnotation(Controller.class)).thenReturn(new LinkedHashMap<>());
        Map<String, Object> restBeans = new LinkedHashMap<>();
        restBeans.put("restController", new DemoRestController());
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(restBeans);

        ScanPermissionPayload payload = new PermissionMenuScanner(applicationContext).scanPreAuthorizeMethods();

        assertThat(payload.getMenuFunctions()).hasSize(1);
        assertThat(payload.getMenuFunctions().get(0).getCode()).isEqualTo("rest-menu");
    }

    @Test
    @DisplayName("类上有 @PermissionMenu 但方法无 @PermissionFunction 时只生成菜单不生成按钮")
    void shouldSkipMethodsWithoutPermissionFunction() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        Map<String, Object> controllerBeans = new LinkedHashMap<>();
        controllerBeans.put("menuOnlyController", new MenuOnlyController());
        when(applicationContext.getBeansWithAnnotation(Controller.class)).thenReturn(controllerBeans);
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(new LinkedHashMap<>());

        ScanPermissionPayload payload = new PermissionMenuScanner(applicationContext).scanPreAuthorizeMethods();

        assertThat(payload.getMenuFunctions()).hasSize(1);
        SysMenuFunction menu = payload.getMenuFunctions().get(0);
        assertThat(menu.getCode()).isEqualTo("menu-only");
        assertThat(menu.getChildren()).isEmpty();
    }

    @Test
    @DisplayName("方法级 menu 覆盖时构建方法所属菜单树")
    void shouldSupportMethodLevelMenuOverride() {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        Map<String, Object> controllerBeans = new LinkedHashMap<>();
        controllerBeans.put("multiMenuController", new MultiMenuController());
        when(applicationContext.getBeansWithAnnotation(Controller.class)).thenReturn(controllerBeans);
        when(applicationContext.getBeansWithAnnotation(RestController.class)).thenReturn(new LinkedHashMap<>());

        ScanPermissionPayload payload = new PermissionMenuScanner(applicationContext).scanPreAuthorizeMethods();

        assertThat(payload.getMenuFunctions()).hasSize(1);
        SysMenuFunction root = payload.getMenuFunctions().get(0);
        assertThat(root.getCode()).isEqualTo("stat");
        assertThat(root.getType()).isEqualTo(PermissionMenuType.CATELOG);
        assertThat(root.getChildren()).hasSize(1);

        SysMenuFunction reportMenu = root.getChildren().get(0);
        assertThat(reportMenu.getCode()).isEqualTo("report");
        assertThat(reportMenu.getType()).isEqualTo(PermissionMenuType.MENU);
        assertThat(reportMenu.getChildren()).hasSize(2);
        assertThat(reportMenu.getChildren()).extracting(SysMenuFunction::getCode)
                .containsExactlyInAnyOrder("export", "import");
    }

    @Controller
    @PermissionMenu(name = "用户菜单", code = "user-menu", parentName = "系统管理", parentCode = "system",
            parentType = PermissionMenuType.CATELOG)
    static class DemoPermissionController {

        @PermissionFunction(name = "查询用户", code = "user-query", permission = "query-user")
        public void queryUser() {
        }

        @PermissionFunction(name = "创建用户", code = "user-create", permission = "create-user")
        public void createUser() {
        }
    }

    @Controller
    static class PlainController {
    }

    @Controller
    @PermissionMenu(name = "重复菜单", code = "dup-code")
    static class DupControllerOne {
    }

    @Controller
    @PermissionMenu(name = "重复菜单", code = "dup-code")
    static class DupControllerTwo {
    }

    @RestController
    @PermissionMenu(name = "REST菜单", code = "rest-menu")
    static class DemoRestController {
    }

    @Controller
    @PermissionMenu(name = "仅菜单", code = "menu-only")
    static class MenuOnlyController {

        public void noAnnotation() {
        }
    }

    @Controller
    @PermissionMenu(name = "")
    static class MultiMenuController {

        @PermissionFunction(name = "导出", code = "export",
                menu = @PermissionMenu(name = "报表管理", code = "report", parentName = "统计", parentCode = "stat"))
        public void export() {
        }

        @PermissionFunction(name = "导入", code = "import",
                menu = @PermissionMenu(name = "报表管理", code = "report", parentName = "统计", parentCode = "stat"))
        public void import1() {
        }
    }


    static class DemoPermissionValueSelector implements PermissionValueSelector {

        @Override
        public String getPermission(Method method) {
            return "permission:" + method.getName();
        }
    }
}
