package com.ddf.boot.common.sharding.rule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.env.MockPropertySource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * LocalRulesCondition 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class LocalRulesConditionTest {

    private final LocalRulesCondition condition = new LocalRulesCondition();
    private final AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

    @Test
    @DisplayName("存在本地规则配置前缀时应命中条件")
    void shouldMatchWhenRulePrefixExists() {
        MockEnvironment environment = new MockEnvironment();
        environment.getPropertySources().addFirst(new MockPropertySource().withProperty(
                "spring.shardingsphere.rules.sharding.tables.user.actual-data-nodes", "ds_${0..1}.user_${0..1}"));

        assertTrue(condition.getMatchOutcome(mockContext(environment), metadata).isMatch());
    }

    @Test
    @DisplayName("数据源地址包含 sharding 时应命中条件")
    void shouldMatchWhenDatasourceUrlContainsSharding() {
        MockEnvironment environment = new MockEnvironment().withProperty("spring.datasource.url",
                "jdbc:shardingsphere:classpath:sharding.yaml");

        assertTrue(condition.getMatchOutcome(mockContext(environment), metadata).isMatch());
    }

    @Test
    @DisplayName("缺少规则配置时不应命中条件")
    void shouldNotMatchWhenNoRuleConfigurationFound() {
        MockEnvironment environment = new MockEnvironment().withProperty("spring.datasource.url",
                "jdbc:mysql://127.0.0.1:3306/demo");

        assertFalse(condition.getMatchOutcome(mockContext(environment), metadata).isMatch());
    }

    private static ConditionContext mockContext(ConfigurableEnvironment environment) {
        ConditionContext context = mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(environment);
        return context;
    }
}
