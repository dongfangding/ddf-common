package com.ddf.common.jwt.config;

import com.ddf.common.jwt.filter.JwtAuthorizationTokenFilter;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Jwt相关配置类
 *
 */
@ConfigurationProperties(prefix = "customs.jwt")
@Data
@NoArgsConstructor
@ConditionalOnBean(JwtAuthorizationTokenFilter.class)
public class JwtProperties {

    public static final String BEAN_NAME = "jwtProperties";


    /**
     * 适用于permitAll的放行路径配置
     * httpSecurity.authorizeRequests().antMatchers(httpMethod, path).permitAll()
     * 如果配置了httpMethod，则针对该方法配置路径；
     * 如果没有配置httpMethod或为*,则直接配置路径
     *
     * @see PathMatch
     *
     */
    private List<PathMatch> permitAllIgnores = new ArrayList<>();

    /**
     * 当token的过期时间小于等于这个时间的时候，服务端重新生成token,
     * 单位 分钟
     */
    private int refreshTokenMinute;

    /**
     * 过期时间，第一次生成token时登录时别的服务生成的，这个过期时间是网关判断token即将失效，重新签发的
     * 单位  分钟
     */
    private int expiredMinute;

    /**
     * 生成的秘钥
     */
    private String secret;


    /**
     * 判断路径是否跳过
     *
     * FIXME 能不能知道ant风格的解析工具类，下面自己写的太简单，问题很多
     * @param path
     * @return
     */
    public boolean isIgnore(String path) {
        if (permitAllIgnores == null || permitAllIgnores.isEmpty()) {
            return false;
        }
        List<String> ignores = permitAllIgnores.stream().filter(s -> StringUtils.isNotBlank(s.getPath()))
                .map(PathMatch::getPath).collect(Collectors.toList());
        // 当前请求地址是否需要跳过认证
        if (!ignores.isEmpty()) {
            for (String ignore : ignores) {
                if (ignore.equals(path)) {
                    return true;
                }
                // 粗略实现ant风格匹配
                if (ignore.contains("**")) {
                    ignore = ignore.substring(0, ignore.indexOf("**") - 1);
                    if (StringUtils.isNotBlank(ignore) && path.startsWith(ignore)) {
                        return true;
                    }
                }
                // 粗略实现ant风格匹配
                if (ignore.contains("*")) {
                    ignore = ignore.substring(0, ignore.indexOf("*") - 1);
                    if (StringUtils.isNotBlank(ignore) && path.startsWith(ignore)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
