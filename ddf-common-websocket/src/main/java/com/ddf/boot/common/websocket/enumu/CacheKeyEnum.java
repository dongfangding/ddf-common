package com.ddf.boot.common.websocket.enumu;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/09/16 16:23
 */
public enum CacheKeyEnum {

    /**
     * 客户端认证信息key
     * {0} loginType
     * {1} accessKeyId
     * {2} authCode
     */
    AUTH_PRINCIPAL_MONITOR("{0}:{1}:{2}")

    ;

    private final String template;

    CacheKeyEnum(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }


}
