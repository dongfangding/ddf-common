package com.ddf.boot.common.websocket.model.ws;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Principal;
import java.util.Objects;


/**
 * 认证用户
 *

 * @date 2019/8/19 16:57
 */
@Data
@NoArgsConstructor
public class AuthPrincipal implements Principal {

    /**
     * 访问身份，
     * 如用户id
     */
    private String accessKeyId;

    /**
     * 随机码
     * 可能需要，也可能不需要，看实际连接的客户端
     * 如果是敏感客户端，可以提供一个绑定操作，让客户端先到服务端申请randomCode，然后才能进行连接
     */
    private String randomCode = "000000";

    /**
     * 登录类型
     */
    private LoginType loginType;

    /**
     * 版本号
     */
    private String version;

    /**
     * 认证时的时间戳，如果拿相同的时间戳去认证一个还在线的相同设备，是不允许挤掉当前在线的设备的；如果不相同，则可以挤掉；
     * 为了防止参数被拿走之后用
     */
    private Long timeStamp;

    public AuthPrincipal(String accessKeyId, String randomCode, LoginType loginType) {
        this.accessKeyId = accessKeyId;
        this.randomCode = randomCode;
        this.loginType = loginType;
    }

    public AuthPrincipal(String accessKeyId, String randomCode, LoginType loginType, String version, long timeStamp) {
        this.accessKeyId = accessKeyId;
        this.randomCode = randomCode;
        this.loginType = loginType;
        this.version = version;
        this.timeStamp = timeStamp;
    }

    /**
     * 构建WEB认证类型
     * @param accessKeyId
     * @param randomCode
     * @return
     */
    public static AuthPrincipal buildAndroidAuthPrincipal(String accessKeyId, String randomCode) {
        return new AuthPrincipal(accessKeyId, randomCode, LoginType.ANDROID);
    }

    /**
     * 构建安卓认证类型
     * @param token
     * @param deviceNumber
     * @return
     */
    public static AuthPrincipal buildAndroidAuthPrincipal(String token, String deviceNumber, String version, long timeStamp) {
        return new AuthPrincipal(token, deviceNumber, LoginType.ANDROID, version, timeStamp);
    }

    /**
     * Returns the name of this principal.
     *
     * @return the name of this principal.
     */
    @Override
    public String getName() {
        return accessKeyId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthPrincipal that = (AuthPrincipal) o;
        return Objects.equals(accessKeyId, that.accessKeyId) &&
                Objects.equals(randomCode, that.randomCode) &&
                loginType == that.loginType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessKeyId, randomCode);
    }

    public enum LoginType {
        /** 安卓认证 */
        ANDROID,
        /** WEB端 认证 */
        WEB;
    }
}
