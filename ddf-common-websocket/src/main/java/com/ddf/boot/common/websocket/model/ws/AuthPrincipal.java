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
     * token
     */
    private String token;

    /**
     * 设备号
     */
    private String deviceNumber;

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

    public AuthPrincipal(String token, String deviceNumber, LoginType loginType) {
        this.token = token;
        this.deviceNumber = deviceNumber;
        this.loginType = loginType;
    }

    public AuthPrincipal(String token, String deviceNumber, LoginType loginType, String version, long timeStamp) {
        this.token = token;
        this.deviceNumber = deviceNumber;
        this.loginType = loginType;
        this.version = version;
        this.timeStamp = timeStamp;
    }

    /**
     * 构建安卓认证类型
     * @param token
     * @param deviceNumber
     * @return
     */
    public static AuthPrincipal buildAndroidAuthPrincipal(String token, String deviceNumber) {
        return new AuthPrincipal(token, deviceNumber, LoginType.ANDROID);
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
        return token;
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
        return Objects.equals(token, that.token) &&
                Objects.equals(deviceNumber, that.deviceNumber) &&
                loginType == that.loginType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, deviceNumber);
    }

    public enum LoginType {
        /** 安卓认证 */
        ANDROID,
        /** APP id认证 */
        APP_ID;
    }
}
