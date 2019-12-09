package com.ddf.boot.common.jwt.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 配置路径放行规则
 *
 */
@ConfigurationProperties("jwt.auth")
@Getter
@Setter
public class IgnorePathMatchProperties {

    public static final String BEAN_NAME = "ignorePathMatchProperties";

    /**
     *
     * @see PathMatch
     *
     */
    private List<PathMatch> ignores = new ArrayList<>();

}
