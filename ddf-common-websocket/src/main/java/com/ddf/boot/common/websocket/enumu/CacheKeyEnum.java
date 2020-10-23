package com.ddf.boot.common.websocket.enumu;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
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
    AUTH_PRINCIPAL_SERVER_MONITOR("auth_principal_server_monitor"),

    /**
     * redis上下线的hash key
     *
     * 批量下线的时候获取AUTH_PRINCIPAL_SERVER_MONITOR节点下的所有key，然后便利符合对应host和port的key,最后批量删除掉key
     *
     * 要么就是不同的host和port维护着各自的在线列表， 但是用的地方又麻烦了
     *
     * {0} server host
     * {1} server port
     * {2} loginType
     * {3} accessKeyId
     * {4} authCode
     */
    AUTH_PRINCIPAL_MONITOR("{0}:{1}:{2}:{3}:{4}")

    ;

    private final String template;

    CacheKeyEnum(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }


}
