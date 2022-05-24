package com.ddf.boot.common.authenticate.config;

import com.ddf.boot.common.core.util.GlobalAntMatcher;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Jwt相关配置类
 *
 * @author dongfang.ding
 * @date 2019-12-07 16:45
 */
@ConfigurationProperties(prefix = "customs.authenticate")
@Data
@NoArgsConstructor
@Component
public class AuthenticateProperties {

    public static final String BEAN_NAME = "authenticateProperties";

    /**
     * 加密算法秘钥
     */
    private String secret;

    /**
     * token header name, 控制客户端通过哪个header传token
     */
    private String tokenHeaderName = "Authorization";

    /**
     * token的value前缀, 控制token是不是包含前缀，token本身不包含，客户端传送过来的时候要包含
     */
    private String tokenPrefix = "Bearer ";

    /**
     * 忽略路径
     */
    private List<String> ignores = new ArrayList<>();

    /**
     * token过期时间
     * 单位  分钟
     */
    private Integer expiredMinute;

    /**
     * 是否开启mock登录用户功能， 如果开启的话，那么则不会走token解析流程，也不走认证流程，而是把token直接作为用户id放入到上下文中直接使用,
     * 则当前请求可以直接非常简单的用对应身份进行操作
     * 需要注意，这个token 仍然需要保持原token格式， 即
     * Authorization：Bearer 1 则当前用户id为1
     */
    private boolean mock;

    /**
     * 为安全起见，必须配置在其中的mock用户才能使用
     */
    private List<String> mockUserIdList;


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
                .anyMatch(ignore -> GlobalAntMatcher.INSTANCE.match(ignore, path));
    }
}
