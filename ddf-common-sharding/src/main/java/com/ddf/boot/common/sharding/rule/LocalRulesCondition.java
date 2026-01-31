/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ddf.boot.common.sharding.rule;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

/**
 * Local rules condition for ShardingSphere.
 *
 * <p>Checks if ShardingSphere rule configuration exists in local file
 * (application.yml/properties) by looking for the prefix "spring.shardingsphere.rules".</p>
 */
public final class LocalRulesCondition extends SpringBootCondition {

    private static final String SHARDING_PREFIX = "spring.shardingsphere.rules";

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext conditionContext, final AnnotatedTypeMetadata annotatedTypeMetadata) {
        ConfigurableEnvironment environment = (ConfigurableEnvironment) conditionContext.getEnvironment();
        // 检查是否包含 ShardingSphere 规则配置前缀
        if (containsPropertyPrefix(environment, SHARDING_PREFIX)) {
            return ConditionOutcome.match();
        }
        // 检查是否包含规则配置文件引用
        String datasourceUrl = environment.getProperty("spring.datasource.url");
        if (datasourceUrl != null && datasourceUrl.contains("sharding")) {
            return ConditionOutcome.match();
        }
        return ConditionOutcome.noMatch("Can't find ShardingSphere rule configuration in local file.");
    }

    /**
     * Check if environment contains property with given prefix.
     *
     * @param environment Spring Environment
     * @param prefix      property prefix
     * @return true if any property starts with the prefix
     */
    private boolean containsPropertyPrefix(final ConfigurableEnvironment environment, final String prefix) {
        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            Object source = propertySource.getSource();
            if (source instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) source;
                for (String key : map.keySet()) {
                    if (key.startsWith(prefix)) {
                        return true;
                    }
                }
            }
        }
        // Fallback: check system properties and environment variables
        String shardingRules = environment.getProperty(SHARDING_PREFIX);
        if (shardingRules != null) {
            return true;
        }
        return false;
    }
}
