package com.ddf.boot.common.api.urlreplace;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * <p>description</p >
 *
 * @author snowball
 * @version 1.0
 * @since 2024/01/08 15:09
 */
@ConfigurationProperties(prefix = "customizer.infra.cloud.static")
@Data
@NoArgsConstructor
@RefreshScope
@Component
public class StaticProperties {

    private String name;

    /**
     * 存储桶对应资源代理地址，可以多个，多个随机取一个使用
     */
    private Map<String, List<String>> resourceProxyHosts;

    /**
     * 忽略的Host, 如果host匹配，则不进行替换
     * 1. 外链
     * 2. 正式环境地址在测试环境用，测试环境忽略替换生成环境域名
     */
    private List<String> ignoreHosts;
}
