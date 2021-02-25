package com.ddf.boot.common.core.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>认证相关参数</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/23 11:10
 */
@Component
@ConfigurationProperties(prefix = "customs.authentication")
@Getter
@Setter
public class AuthenticationProperties {

    /**
     * 登录白名单
     * 不进行验证码校验
     */
    private List<String> whiteLoginNameList;

    /**
     * 重置后默认密码
     */
    private String resetPassword;

}
