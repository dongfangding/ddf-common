package com.ddf.common.boot.mqtt.config;

import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.boot.common.core.helper.ThreadBuilderHelper;
import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.common.boot.mqtt.client.DefaultMqttPublishImpl;
import com.ddf.common.boot.mqtt.client.MqttDefinition;
import com.ddf.common.boot.mqtt.client.MqttPublishClient;
import com.ddf.common.boot.mqtt.config.properties.EmqConnectionProperties;
import com.ddf.common.boot.mqtt.controller.EmqController;
import com.ddf.common.boot.mqtt.enume.MQTTProtocolEnum;
import com.ddf.common.boot.mqtt.enume.MqttQosEnum;
import com.ddf.common.boot.mqtt.exception.MqttCallbackCode;
import com.ddf.common.boot.mqtt.extra.MqttPublishListener;
import com.ddf.common.boot.mqtt.extra.impl.MqttPublishCheckerListener;
import com.ddf.common.boot.mqtt.support.GlobalStorage;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * MQTT Client 配置类
 * </p>
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/03/18 13:54
 */
@AutoConfiguration
@EnableConfigurationProperties(value = {EmqConnectionProperties.class})
@Slf4j
@Import(EmqController.class)
public class MqttAutoConfiguration implements DisposableBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * 创建Mqtt客户端
     *
     * @return MqttClient
     */
    @Bean
    @ConditionalOnProperty(prefix = "customizer.infra.mqtt", value = "enable", havingValue = "true")
    public MqttAsyncClient mqttClient(EmqConnectionProperties emqConnectionProperties, EnvironmentHelper environmentHelper) {
        // 获取客户端配置
        final EmqConnectionProperties.ClientConfig clientConfig = emqConnectionProperties.getClient();
        PreconditionUtil.checkArgument(
                Objects.nonNull(clientConfig), MqttCallbackCode.MQTT_CONFIG_CONNECTION_CLIENT_MISS);
        // 存入到全局变量中
        GlobalStorage.clientConfig = clientConfig;
        GlobalStorage.SYSTEM_CLIENT_ID_PREFIX = clientConfig.getClientIdPrefix();
        GlobalStorage.APPLICATION_PORT = environmentHelper.getPort();

        // 默认使用mqtt 的 tcp 来进行连接
        final String protocol = MQTTProtocolEnum.MQTT_TCP.getProtocol();
        final EmqConnectionProperties.ConnectionConfig connectionConfig = emqConnectionProperties.getConnectionUrl(
                protocol);
        PreconditionUtil.checkArgument(
                Objects.nonNull(connectionConfig), MqttCallbackCode.MQTT_CONFIG_CONNECTION_TCP_PROTOCOL_ERROR);

        final String url = connectionConfig.getUrl();
        MqttAsyncClient mqttClient;
        try {
            // 发送消息质量大于0的消息时， 在broker未回执前，会存在内存中，new MemoryPersistence()，这样速度极快，但需要注意分配足够的内存。
            // 如果使用磁盘，性能会下降。
            mqttClient = new MqttAsyncClient(url, emqConnectionProperties.getClientId(), new MemoryPersistence());
        } catch (MqttException e) {
            log.error("mqtt tcp 创建客户端失败， protocol = {}, url = {}", protocol, url, e);
            throw new BusinessException(MqttCallbackCode.MQTT_CONFIG_CREATE_CLIENT_ERROR);
        }

        // 客户端连接配置
        MqttConnectionOptions connOpts = new MqttConnectionOptions();
        connOpts.setUserName(clientConfig.getUsername());
        connOpts.setPassword(clientConfig
                .getPassword()
                .getBytes(StandardCharsets.UTF_8));
        connOpts.setKeepAliveInterval(60);
        connOpts.setConnectionTimeout(30);
        // 最大重连延迟10秒
        connOpts.setMaxReconnectDelay(10000);
        // 重连后，清除之前的会话信息。因为目前使用的内存new MemoryPersistence()来处理qos>0的消息回执状态， 不清楚的话，可能会内存爆掉。
        // 如果对消息质量要求比较高，同时启动磁盘来处理消息的话，这里可以改为false，这样即使重启也能继续处理之前的消息
        connOpts.setCleanStart(true);
        // 设置最大在途消息数，压测时发现，如果qos质量大于1，每个client都必须等待broker ack， 达到一定数量，就会抛异常 org.eclipse.paho.mqttv5.common.MqttException: 正在进行过多的发布
        // 同时还要调整emqx控制台的会话里的“最大飞行窗口”
        connOpts.setReceiveMaximum(10000);
        connOpts.setAutomaticReconnect(true);
        // 设置回调要在 connect 之前，确保不会丢失首次连接成功的通知
        setupMqttCallback(mqttClient);
        try {
            mqttClient.connect(connOpts);
        } catch (MqttException e) {
            log.warn("mqtt tcp 连接客户端失败， 将由后台自动重连尝试: protocol = {}, url = {}", protocol, url, e);
        }
        return mqttClient;
    }
    /**
     * @param mqttClient 参数
     */
    private void setupMqttCallback(MqttAsyncClient mqttClient) {
        mqttClient.setCallback(new MqttCallback() {
            /**
             * 连接断开回调
             * @param disconnectResponse 参数
             */
            @Override
            public void disconnected(MqttDisconnectResponse disconnectResponse) {
                log.error(
                        "mqtt tcp 重新连接客户端失败， returnCode = {}, reasonString = {}",
                        disconnectResponse.getReturnCode(), disconnectResponse.getReasonString()
                );
            }
            /**
             * @param exception 参数
             */
            @Override
            public void mqttErrorOccurred(MqttException exception) {
                log.error("mqtt 运行异常", exception);
            }

            /**
             * 当消息从服务器到达时会回到该方法，该方法是是由mqtt服务区同步调用的，这个方法没结束，
             * 不会将确认消息发送回服务器
             * 如果该方法抛出了异常，客户端将会被断开连接，当客户端重新再次连接时，任何消息质量qos为1和2的消息都会被重新从服务器发送
             *
             * 如果应用需要持久化数据，那么在从这个方法返回之前，应该保证数据是持久化的，因为从这个方法返回之后，就认为消息已经送达了，并且无法重现
             *
             * @param topic   主题参数
             * @param message 消息内容
             * @throws Exception if a terminal error has occurred, and the client should be
             *                   shut down.
             */
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // 如果当前模块需要订阅消息， 则是实现这个方法，目前只作为发送客户端封装
            }

            /**
             * 当消息的传递完成并收到所有确认时调用。对于 QoS 0 消息，一旦将消息交给网络进行传递，就会调用它。对于 QoS 1，
             * 它在收到 PUBACK 时被调用，对于 QoS 2，它在收到 PUBCOMP 时被调用。令牌将与发布消息时返回的令牌相同
             *
             * @param token token 字符串
             */
            @Override
            public void deliveryComplete(IMqttToken token) {
                // 消息确认
            }
            /**
             * @param reconnect 参数
             * @param serverURI 参数
             */
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                log.info("mqtt 连接成功: {}, 是否为重连: {}", serverURI, reconnect);
            }
            /**
             * @param reasonCode 参数
             * @param properties 参数
             */
            @Override
            public void authPacketArrived(int reasonCode, MqttProperties properties) {

            }
        });
    }

    /**
     * 构建 QoS 等级到线程池的映射，O(1) 时间复杂度获取，消除运行时查找开销
     *
     * @param qos0Executor QoS 0 线程池
     * @param qos1Executor QoS 1 线程池
     * @param qos2Executor QoS 2 线程池
     * @return QoS 线程池映射
     */
    @Bean
    @ConditionalOnProperty(prefix = "customizer.infra.mqtt", value = "enable", havingValue = "true")
    public Map<MqttQosEnum, ThreadPoolTaskExecutor> mqttQosExecutors(
            @Qualifier("qos0Executors") ThreadPoolTaskExecutor qos0Executor,
            @Qualifier("qos1Executors") ThreadPoolTaskExecutor qos1Executor,
            @Qualifier("qos2Executors") ThreadPoolTaskExecutor qos2Executor) {
        return Map.of(
                MqttQosEnum.AT_MOST_ONCE, qos0Executor, MqttQosEnum.AT_LAST_ONCE, qos1Executor,
                MqttQosEnum.EXACTLY_ONCE, qos2Executor
        );
    }

    /**
     * MQTT 消息发送重试模板，指数退避策略
     *
     * @return RetryTemplate
     */
    @Bean
    @ConditionalOnProperty(prefix = "customizer.infra.mqtt", value = "enable", havingValue = "true")
    public RetryTemplate mqttRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 指数退避策略：初始间隔100ms，最大间隔5s，倍数2.0
        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(100);
        backOff.setMaxInterval(5000);
        backOff.setMultiplier(2.0);
        retryTemplate.setBackOffPolicy(backOff);

        // 最大重试3次
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    /**
     * MQTT 内部实现 bean
     *
     * @param mqttAsyncClient              MQTT 客户端
     * @param listenerMap             发布监听器映射
     * @param emqConnectionProperties MQTT 配置属性
     * @param qosExecutors            QoS 线程池映射
     * @param retryTemplate           重试模板
     * @return MQTT 定义接口实现
     */
    @Bean
    @ConditionalOnProperty(prefix = "customizer.infra.mqtt", value = "enable", havingValue = "true")
    public MqttDefinition mqttDefinition(MqttAsyncClient mqttAsyncClient,
            ObjectProvider<Map<String, MqttPublishListener>> listenerMap,
            EmqConnectionProperties emqConnectionProperties, Map<MqttQosEnum, ThreadPoolTaskExecutor> qosExecutors,
            RetryTemplate retryTemplate) {
        return new DefaultMqttPublishImpl(
                mqttAsyncClient, listenerMap.getIfAvailable(), emqConnectionProperties,
                qosExecutors, retryTemplate
        );
    }

    /**
     * qos 0 消息发送线程池
     *
     * @return ThreadPoolTaskExecutor
     */
    @Bean
    @ConditionalOnMissingBean(name = "qos0Executors")
    public ThreadPoolTaskExecutor qos0Executors() {
        return ThreadBuilderHelper.buildThreadExecutor("qos0-executors-", 10, 100);
    }

    /**
     * qos 1 消息发送线程池
     *
     * @return ThreadPoolTaskExecutor
     */
    @Bean
    @ConditionalOnMissingBean(name = "qos1Executors")
    public ThreadPoolTaskExecutor qos1Executors() {
        return ThreadBuilderHelper.buildThreadExecutor("qos1-executors-", 10, 100);
    }

    /**
     * qos 2 消息发送线程池
     *
     * @return ThreadPoolTaskExecutor
     */
    @Bean
    @ConditionalOnMissingBean(name = "qos2Executors")
    public ThreadPoolTaskExecutor qos2Executors() {
        return ThreadBuilderHelper.buildThreadExecutor("qos2-executors-", 10, 100);
    }

    /**
     * 暴露给外部使用的封装好的发送消息的 client
     *
     * @param mqttDefinition MQTT 定义接口
     * @return MQTT 发布客户端
     */
    @Bean
    @ConditionalOnProperty(prefix = "customizer.infra.mqtt", value = "enable", havingValue = "true")
    public MqttPublishClient mqttPublishClient(MqttDefinition mqttDefinition) {
        return new MqttPublishClient(mqttDefinition);
    }

    @Bean
    public MqttPublishCheckerListener mqttPublishCheckerListener(EmqConnectionProperties emqConnectionProperties) {
        return new MqttPublishCheckerListener(emqConnectionProperties);
    }

    @Override
    public void destroy() throws Exception {
        // 修复 NPE 风险：使用 getBean 可能抛出异常，改用安全的方式获取
        if (applicationContext == null) {
            log.warn("ApplicationContext 为空，跳过 MQTT client 关闭");
            return;
        }
        try {
            final MqttClient mqttClient = applicationContext.getBean(MqttClient.class);
            if (Objects.nonNull(mqttClient) && mqttClient.isConnected()) {
                mqttClient.disconnect();
                log.info("MQTT client 已成功断开连接");
            }
        } catch (BeansException e) {
            log.warn("获取 MQTT client bean 失败，可能已被移除: {}", e.getMessage());
        } catch (MqttException e) {
            log.error("断开 MQTT 连接失败", e);
        }
    }
    /**
     * @param context 参数
     */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = context;
    }
}
