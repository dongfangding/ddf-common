package com.ddf.boot.common.websocket.model.ws;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Principal;
import java.util.Objects;


/**
 * 认证用户
 *
 *
 * @author dongfang.ding
 * @date 2019/12/21
 */
@Data
@NoArgsConstructor
public class AuthPrincipal implements Principal {

    /**
     * randomCode,服务端对设备号生成的随机码
     */
    private String randomCode;

    /**
     * 设备号
     */
    private String ime;

    /**
     * 登录类型
     */
    private LoginType loginType;

    public AuthPrincipal(String randomCode, String ime, LoginType loginType) {
        this.randomCode = randomCode;
        this.ime = ime;
        this.loginType = loginType;
    }

    /**
     * 构建安卓认证类型
     * @param token
     * @param ime
     * @return
     */
    public static AuthPrincipal buildAndroidAuthPrincipal(String token, String ime) {
        return new AuthPrincipal(token, ime, LoginType.ANDROID);
    }

    /**
     * Returns the name of this principal.
     *
     * @return the name of this principal.
     */
    @Override
    public String getName() {
        return ime;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthPrincipal that = (AuthPrincipal) o;
        return Objects.equals(randomCode, that.randomCode) &&
                Objects.equals(ime, that.ime) &&
                loginType == that.loginType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(randomCode, ime);
    }

    public enum LoginType {
        /** 安卓认证 */
        ANDROID,
        /** APP id认证 */
        APP_ID



        ;
    }
}
