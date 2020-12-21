package com.ddf.boot.common.websocket.model;

import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 认证用户
 *
 * @author dongfang.ding
 * @date 2019/8/19 16:57
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthPrincipal implements Principal, Serializable {

    static final long serialVersionUID = 42L;

    /**
     * 默认身份授权码
     */
    public static final String DEFAULT_AUTH_CODE = "000000";

    /**
     * 认证身份关键字，标识客户端身份的唯一标识符
     */
    private String accessKeyId;

    /**
     * 认证身份的名称
     */
    private String accessKeyName;

    /**
     * 预留的其它用于表示身份的补充信息
     */
    private String authCode = DEFAULT_AUTH_CODE;

    /**
     * 登录类型
     */
    private LoginType loginType;

    /**
     * 版本号
     * 预留字段
     */
    private String version = "1.0";

    /**
     * 认证时的时间戳，如果拿相同的时间戳去认证一个还在线的相同设备，是不允许挤掉当前在线的设备的；如果不相同，则可以挤掉；
     * 为了防止参数被拿走之后用
     */
    private Long timeStamp;

    public AuthPrincipal(String accessKeyId, String authCode, LoginType loginType) {
        this.accessKeyId = accessKeyId;
        this.authCode = authCode;
        this.loginType = loginType;
    }

    public AuthPrincipal(String accessKeyId, String accessKeyName, String authCode, LoginType loginType, String version,
            long timeStamp) {
        this.accessKeyId = accessKeyId;
        this.accessKeyName = accessKeyName;
        this.authCode = authCode;
        this.loginType = loginType;
        this.version = version;
        this.timeStamp = timeStamp;
    }

    /**
     * 构建用于通讯的关键身份信息类型, 一般为服务端用，通过这个找对应的连接
     *
     * @param accessKeyId
     * @return
     */
    public static AuthPrincipal buildChannelPrincipal(String accessKeyId, String authCode, LoginType loginType) {
        return new AuthPrincipal(accessKeyId, authCode, loginType);
    }

    /**
     * 构建用于通讯的关键身份信息类型, 一般为服务端用，通过这个找对应的连接
     *
     * @param accessKeyId
     * @return
     */
    public static AuthPrincipal buildChannelPrincipal(String accessKeyId, LoginType loginType) {
        return new AuthPrincipal(accessKeyId, DEFAULT_AUTH_CODE, loginType);
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
        return Objects.equals(accessKeyId, that.accessKeyId) && Objects.equals(authCode, that.authCode)
                && loginType == that.loginType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessKeyId, authCode);
    }

    public enum LoginType {

        /**
         * 用户认证
         */
        USER,

        /**
         * 设备认证
         * 如安卓、苹果手机或其他终端设备
         */
        DEVICE,

    }
}
