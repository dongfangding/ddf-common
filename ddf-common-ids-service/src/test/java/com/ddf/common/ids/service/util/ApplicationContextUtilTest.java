package com.ddf.common.ids.service.util;

import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * ApplicationContextUtil 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class ApplicationContextUtilTest {

    private final ApplicationContextUtil applicationContextUtil = new ApplicationContextUtil();

    @AfterEach
    void tearDown() {
        applicationContextUtil.setApplicationContext(null);
    }

    @Test
    @DisplayName("应支持按名称和类型获取 Bean")
    void shouldGetBeanByNameAndType() {
        StaticApplicationContext context = new StaticApplicationContext();
        context.getBeanFactory().registerSingleton("demoBean", new DemoBean("demo"));
        context.refresh();
        applicationContextUtil.setApplicationContext(context);

        assertSame(context, ApplicationContextUtil.getApplicationContext());
        assertEquals("demo", ((DemoBean) ApplicationContextUtil.getBean("demoBean")).getName());
        assertEquals("demo", ApplicationContextUtil.getBean(DemoBean.class).getName());
        assertEquals("demo", ApplicationContextUtil.getBean("demoBean", DemoBean.class).getName());
    }

    @Test
    @DisplayName("应支持按类型获取多个 Bean")
    void shouldGetBeansOfType() {
        StaticApplicationContext context = new StaticApplicationContext();
        context.getBeanFactory().registerSingleton("demoBeanA", new DemoBean("A"));
        context.getBeanFactory().registerSingleton("demoBeanB", new DemoBean("B"));
        context.refresh();
        applicationContextUtil.setApplicationContext(context);

        Map<String, DemoBean> beans = ApplicationContextUtil.getBeansOfType(DemoBean.class);

        assertEquals(2, beans.size());
        assertEquals("A", beans.get("demoBeanA").getName());
        assertEquals("B", beans.get("demoBeanB").getName());
    }

    static class DemoBean {
        private final String name;

        DemoBean(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
