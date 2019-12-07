package com.ddf.common.jwt.config;

import com.ddf.common.jwt.filter.JwtAuthorizationTokenFilter;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
@ConditionalOnBean(JwtAuthorizationTokenFilter.class)
public class IgnorePathMatchProperties {

    public static final String BEAN_NAME = "ignorePathMatchProperties";

    /**
     *
     * @see PathMatch
     *
     */
    private List<PathMatch> ignores = new ArrayList<>();

}
