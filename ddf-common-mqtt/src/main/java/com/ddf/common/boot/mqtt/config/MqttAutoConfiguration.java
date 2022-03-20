package com.ddf.common.boot.mqtt.config;

import com.ddf.boot.common.core.exception200.BusinessException;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.common.boot.mqtt.client.DefaultMqttPublishImpl;
import com.ddf.common.boot.mqtt.client.MqttDefinition;
import com.ddf.common.boot.mqtt.client.MqttPublishClient;
import com.ddf.common.boot.mqtt.config.properties.EmqConnectionProperties;
import com.ddf.common.boot.mqtt.enume.MQTTProtocolEnum;
import com.ddf.common.boot.mqtt.exception.MqttCallbackCode;
import com.ddf.common.boot.mqtt.support.GlobalStorage;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>mqtt client 配置类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/18 13:54
 */
@Configuration
@EnableConfigurationProperties(value = {EmqConnectionProperties.class})
@Slf4j
public class MqttAutoConfiguration implements DisposableBean, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private EnvironmentHelper environmentHelper;

    /**
     * 创建Mqtt客户端
     *
     * @return
     */
    @Bean
    public MqttClient mqttClient(EmqConnectionProperties emqConnectionProperties) {
        // 获取客户端配置
        final EmqConnectionProperties.ClientConfig clientConfig = emqConnectionProperties.getClient();
        PreconditionUtil.checkArgument(Objects.nonNull(clientConfig), MqttCallbackCode.MQTT_CONFIG_CONNECTION_CLIENT_MISS);
        // 存入到全局变量中
        GlobalStorage.clientConfig = clientConfig;
        GlobalStorage.SYSTEM_CLIENT_ID_PREFIX = clientConfig.getClientIdPrefix();
        GlobalStorage.APPLICATION_PORT = environmentHelper.getPort();

        // 默认使用mqtt 的 tcp 来进行连接
        final String protocol = MQTTProtocolEnum.MQTT_TCP.getProtocol();
        final EmqConnectionProperties.ConnectionConfig connectionConfig = emqConnectionProperties.getConnectionUrl(protocol);
        PreconditionUtil.checkArgument(Objects.nonNull(connectionConfig), MqttCallbackCode.MQTT_CONFIG_CONNECTION_TCP_PROTOCOL_ERROR);

        final String url = connectionConfig.getUrl();
        MqttClient mqttClient;
        try {
            mqttClient = new MqttClient(url, emqConnectionProperties.getClientId(), new MemoryPersistence());
        } catch (MqttException e) {
            log.error("mqtt tcp 创建客户端失败， protocol = {}, url = {}", protocol, url, e);
            throw new BusinessException(MqttCallbackCode.MQTT_CONFIG_CREATE_CLIENT_ERROR);
        }

        // 客户端连接配置
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setUserName(clientConfig.getUsername());
        connOpts.setPassword(clientConfig.getPassword().toCharArray());
        connOpts.setKeepAliveInterval(60);
        connOpts.setMaxInflight(10);
        connOpts.setConnectionTimeout(0);
        connOpts.setCleanSession(true);
        connOpts.setAutomaticReconnect(true);
        try {
            mqttClient.connect(connOpts);
        } catch (MqttException e) {
            log.error("mqtt tcp 连接客户端失败， protocol = {}, url = {}", protocol, url, e);
            throw new BusinessException(MqttCallbackCode.MQTT_CONFIG_CONNECTION_ERROR);
        }

        // 设置回调
        MqttClient finalMqttClient = mqttClient;
        mqttClient.setCallback(new MqttCallback() {

            /**
             * 连接断开回调
             *
             * @param cause the reason behind the loss of connection.
             */
            @Override
            public void connectionLost(Throwable cause) {
                try {
                    finalMqttClient.reconnect();
                } catch (MqttException e) {
                    log.error("mqtt tcp 重新连接客户端失败， protocol = {}, url = {}", protocol, url, e);
                }
            }

            /**
             * 当消息从服务器到达时会回到该方法，该方法是是由mqtt服务区同步调用的，这个方法没结束，
             * 不会将确认消息发送回服务器
             * 如果该方法抛出了异常，客户端将会被断开连接，当客户端重新再次连接时，任何消息质量qos为1和2的消息都会被重新从服务器发送
             *
             * 如果应用需要持久化数据，那么在从这个方法返回之前，应该保证数据是持久化的，因为从这个方法返回之后，就认为消息已经送达了，并且无法重现
             *
             * @param topic   name of the topic on the message was published to
             * @param message the actual message.
             * @throws Exception if a terminal error has occurred, and the client should be
             *                   shut down.
             */
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // 消息是否可以在此持久化？
            }

            /**
             * 当消息的传递完成并收到所有确认时调用。对于 QoS 0 消息，一旦将消息交给网络进行传递，就会调用它。对于 QoS 1，
             * 它在收到 PUBACK 时被调用，对于 QoS 2，它在收到 PUBCOMP 时被调用。令牌将与发布消息时返回的令牌相同
             *
             * @param token the delivery token associated with the message.
             */
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // 消息确认
            }
        });
        return mqttClient;
    }

    /**
     * mqtt内部实现bean
     *
     * @param mqttClient
     * @return
     */
    @Bean
    public MqttDefinition mqttDefinition(MqttClient mqttClient) {
        return new DefaultMqttPublishImpl(mqttClient);
    }

    /**
     * 暴露给外部使用的封装好的发送消息的client
     *
     * @param mqttDefinition
     * @return
     */
    @Bean
    public MqttPublishClient mqttPublishClient(MqttDefinition mqttDefinition) {
        return new MqttPublishClient(mqttDefinition);
    }

    @Override
    public void destroy() throws Exception {
        // 关闭mqtt client
        final MqttClient mqttClient = applicationContext.getBean(MqttClient.class);
        if (mqttClient.isConnected()) {
            mqttClient.disconnect();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
