package com.ddf.common.ons.console.constant;

import java.util.Objects;

/**
 * <p>console相关常量类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/17 15:28
 */
public interface ConsoleConstants {

    /**
     * 多个环境的{@link com.aliyun.ons20190214.Client} bean name生成规则 env + 当前字符
     */
    String CLIENT_BEAN_NAME_SUFFIX = "OnsConsoleClient";

    /**
     * 重试表达式固定前缀， 用以一些判断，如判断一个表达式是否是重试表达式
     */
    String RETRY_EXPRESSION_SUFFIX = "&RETRY_EXPRESSION";

    /**
     * 获取对应环境的{@link com.aliyun.ons20190214.Client}  bean_name
     *
     * @param env
     * @return
     */
    static String getOnsClientBeanName(String env) {
        return env + CLIENT_BEAN_NAME_SUFFIX;
    }

    /**
     * 判断表达式是否是重试表达式
     *
     * @param expression
     * @return
     */
    static boolean isRetryExpression(String expression) {
        return Objects.nonNull(expression) && expression.startsWith(RETRY_EXPRESSION_SUFFIX);
    }
}
