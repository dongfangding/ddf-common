package com.ddf.common.boot.mqtt.config.properties;

import cn.hutool.core.net.NetUtil;
import com.ddf.common.boot.mqtt.enume.MQTTProtocolEnum;
import com.ddf.common.boot.mqtt.support.GlobalStorage;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * <p>mqtt连接配置类</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/03/18 13:46
 */
@ConfigurationProperties(prefix = "customizer.infra.mqtt")
@Data
public class EmqConnectionProperties {

    /**
     * 是否开启
     */
    private boolean enable;

    /**
     * 连接地址
     */
    private List<ConnectionConfig> connectionUrls;

    /**
     * 客户端配置
     */
    private ClientConfig client;


    /**
     * 最大payload大小，默认256k, 由于原生emq超过大小会断开连接，这里做一个拦截， 避免错误超过大小，将连接断开了。
     * 但是要注意，emq自己的设置大小一定要大于这里的值。
     */
    private Integer maxPayloadSize = 256 * 1024;

    /**
     * 单机发布消息限流
     */
    private Integer publishRateLimit = 5000;

    /**
     * qos = 0 消息执行线程池bean name, 如果不指定则为默认的
     */
    private String qos0Executors = "qos0Executors";
    /**
     * qos = 1 消息执行线程池bean name
     */
    private String qos1Executors = "qos1Executors";

    /**
     * qos = 2 消息执行线程池bean name
     */
    private String qos2Executors = "qos2Executors";



    /**
     * 连接配置类
     */
    @Data
    public static class ConnectionConfig implements Serializable {

        private static final long serialVersionUID = 1516322558409231083L;

        /**
         * 协议
         *
         * @see com.ddf.common.boot.mqtt.enume.MQTTProtocolEnum#getProtocol()
         */
        private String protocol;

        /**
         * 连接地址
         * tcp://localhost:1883
         */
        private String url;
    }


    /**
     * 客户端配置，这里主要是给服务端创建MqttClient用的
     */
    @Data
    public static class ClientConfig {
        /**
         * 服务端构建MqttClient时使用的clientId的前缀， 实际使用每次连接都是附带当前时间戳
         */
        private String clientIdPrefix = "DefaultClientId";

        /**
         * 连接用的用户名
         */
        private String username;

        /**
         * 连接用的密码
         */
        private String password;
    }

    /**
     * 获取指定协议的配置
     *
     * @param protocol protocol参数
     * @return
     */
    public ConnectionConfig getConnectionUrl(String protocol) {
        Map<String, ConnectionConfig> protocolMap = connectionUrls
                .stream()
                .collect(Collectors.toMap(ConnectionConfig::getProtocol, obj -> obj));
        return protocolMap.get(protocol);
    }

    /**
     * 获取服务端使用的clientId
     *
     * @return
     */
    public String getClientId() {
        return String.join(
                "-", getClient().getClientIdPrefix(), NetUtil.getLocalhostStr() + "",
                GlobalStorage.APPLICATION_PORT + ""
        );
    }
}
