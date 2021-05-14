package com.ddf.boot.common.jwt.config;

import com.ddf.boot.common.jwt.util.JwtUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Jwt相关配置类
 *
 * @author dongfang.ding
 * @date 2019-12-07 16:45
 */
@ConfigurationProperties(prefix = "customs.jwt")
@Data
@NoArgsConstructor
@Component
public class JwtProperties {

    public static final String BEAN_NAME = "jwtProperties";

    /**
     * 适用于permitAll的放行路径配置
     * httpSecurity.authorizeRequests().antMatchers(httpMethod, path).permitAll()
     * 如果配置了httpMethod，则针对该方法配置路径；
     * 如果没有配置httpMethod或为*,则直接配置路径
     *
     * @see PathMatch
     */
    private List<PathMatch> ignores = new ArrayList<>();

    /**
     * 当token的过期时间小于等于这个时间的时候，服务端重新生成token,
     * 单位 分钟
     */
    private int refreshTokenMinute;

    /**
     * 过期时间，签发jwt的过期时间
     * 单位  分钟
     */
    private int expiredMinute;

    /**
     * 加密算法秘钥
     */
    private String secret;

    /**
     * 是否开启mock登录用户功能， 如果开启的话，那么则不会走jwt的解析流程，也不走认证流程，而是把token直接作为用户id放入到上下文中直接使用,
     * 则当前请求可以直接非常简单的用对应身份进行操作
     */
    private boolean mock;


    /**
     * 判断路径是否跳过
     *
     * @param path
     * @return
     */
    public boolean isIgnore(String path) {
        if (ignores == null || ignores.isEmpty()) {
            return false;
        }
        // 当前请求地址是否需要跳过认证
        return ignores.stream()
                .map(PathMatch::getPath)
                .filter(StringUtils::isNotBlank)
                .anyMatch(ignore -> JwtUtil.getAntPathMatcher().match(ignore, path));
    }
}
