package com.ddf.boot.common.authentication.config;

import com.ddf.boot.common.api.model.common.request.RequestHeaderEnum;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 认证模块相关配置类
 *
 * @author dongfang.ding
 * @date 2019-12-07 16:45
 */
@ConfigurationProperties(prefix = "customs.ext.authentication")
@Data
@NoArgsConstructor
@RefreshScope
@Component
public class AuthenticationProperties {

    public static final String BEAN_NAME = "cloudAuthenticationProperties";

    /**
     * 加密算法秘钥
     */
    private String secret;

    /**
     * token header name, 控制客户端通过哪个header传token
     */
    private String tokenHeaderName = "ACCESS-TOKEN";

    /**
     * token的value前缀, 控制token是不是包含前缀，token本身不包含，客户端传送过来的时候要包含
     */
    private String tokenPrefix = "";

    /**
     * 用来校验token中credit身份的header name, 如果生成token的时候不放入，则不会校验
     * 如果是移动端，一般是设备号什么之类的
     */
    private String creditHeaderName = RequestHeaderEnum.IMEI.getName();

    /**
     * 忽略路径
     */
    private List<String> ignores = new ArrayList<>();

    /**
     * 针对开放平台的忽略路径，不校验任何逻辑，直接转发请求
     */
    private List<String> openIgnores = new ArrayList<>();

    /**
     * token过期时间
     * 单位  分钟
     */
    private Integer expiredMinute;

    /**
     * 加签秘钥
     */
    private String signSecret = "abcdefghijklmnopqrstuvw987654321";

    /**
     * 加签验证是否开启
     */
    private boolean signEnabled = false;


    /**
     * 是否开启万能sign
     */
    private boolean mockSignEnabled;

    /**
     * 万能sign， 某些情况下使用
     */
    private String mockSign;
}





