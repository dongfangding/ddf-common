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
@ConfigurationProperties(prefix = "customs.cloud.static")
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
}
