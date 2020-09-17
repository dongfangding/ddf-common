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
     * redis上下线的key
     * {0} 服务端主机:端口号  如果是集群，采用其它机器感知掉线服务器，然后删除对应服务器上的所有连接信息， 如果是单机，每台服务重启的时候将当前主机维护的在线客户端清空；
     *              单机重启的时的数据不一致不影响业务，因为服务挂了，不会执行任何请求
     *
     */
    AUTH_PRINCIPAL_SERVER_MONITOR("auth_principal_server_monitor:{0}:{1}"),

    /**
     * redis上下线的hash key
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
