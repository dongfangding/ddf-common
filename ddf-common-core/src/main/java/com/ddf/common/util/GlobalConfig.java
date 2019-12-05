package com.ddf.common.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 * 存放一些全局的自定义属性，根据需要决定是否可配置
 *
 * @author dongfang.ding on 2019/1/25
 */
@Component
@ConfigurationProperties(prefix = "customs.global-config")
@Getter
@Setter
public class GlobalConfig {

    private String anonymousName;

    private String initPassword;

    private String platformCompCode;
}
