package com.ddf.common.boot.mqtt.config.properties;

import cn.hutool.core.net.NetUtil;
import com.ddf.common.boot.mqtt.enume.MQTTProtocolEnum;
import com.ddf.common.boot.mqtt.support.GlobalStorage;
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
 * @date 2022/03/18 13:46
 */
@ConfigurationProperties(prefix = "emq.config")
@Data
public class EmqConnectionProperties {

    /**
     * 连接地址
     */
    private List<ConnectionConfig> connectionUrls;

    /**
     * 客户端配置
     */
    private ClientConfig client;

    /**
     * 连接配置类
     */
    @Data
    public static class ConnectionConfig {

        /**
         * 协议
         * @see MQTTProtocolEnum#getProtocol()
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
     * @param protocol
     * @return
     */
    public ConnectionConfig getConnectionUrl(String protocol) {
        Map<String, ConnectionConfig> protocolMap = connectionUrls.stream().collect(Collectors.toMap(ConnectionConfig::getProtocol, obj -> obj));
        return protocolMap.get(protocol);
    }

    /**
     * 获取服务端使用的clientId
     *
     * @return
     */
    public String getClientId() {
        return String.join("-", getClient().getClientIdPrefix(), NetUtil.getLocalhostStr() + "",
                GlobalStorage.APPLICATION_PORT + "");
    }
}
