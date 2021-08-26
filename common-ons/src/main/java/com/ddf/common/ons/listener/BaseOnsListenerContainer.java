package com.ddf.common.ons.listener;

import com.aliyun.openservices.ons.api.Admin;
import com.aliyun.openservices.ons.api.ExpressionType;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.batch.BatchMessageListener;
import com.aliyun.openservices.ons.api.bean.BatchConsumerBean;
import com.aliyun.openservices.ons.api.bean.ConsumerBean;
import com.aliyun.openservices.ons.api.bean.OrderConsumerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.ddf.common.ons.annotation.OnsMessageListener;
import com.ddf.common.ons.enume.ConsumeMode;
import com.ddf.common.ons.enume.MessageModel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基础ONS的监听器容器， 其它容器都是依照此类作为模板根据注解来确定要创建新的监听器
 *
 * @author snowball
 * @date 2021/8/26 14:37
 **/
@Data
public class BaseOnsListenerContainer implements OnsListenerContainer {

    private final static Logger LOGGER = LoggerFactory.getLogger(BaseOnsListenerContainer.class);

    /**
     * 监听器容器Bean名称
     */
    private String name;
    /**
     * ONS请求key
     */
    private String accessKey;
    /**
     * ONS密钥
     */
    private String secretKey;
    /**
     * ONS TCP接入域名
     */
    private String nameServerAddr;

    private OnsMessageListener onsMessageListener;

    private MessageListener messageListener;

    private BatchMessageListener batchMessageListener;

    private MessageOrderListener messageOrderListener;

    private ConsumeMode consumeMode;

    private Admin admin;

    private OnsMessageListener annotation;

    // The following properties came from @OnsMessageListener.
    private String groupId;
    private String topic;
    private ExpressionType expressionType;
    private String expression;
    private MessageModel messageModel;
    private int consumeThreadNums;
    private long consumeTimeout;
    private int consumeMessageBatchMaxSize;
    private boolean running;

    /**
     * 解析出annotation中的值放入容器中
     *
     * @param annotation
     */
    public void setAnnotation(OnsMessageListener annotation) {
        this.annotation = annotation;
        this.groupId = annotation.groupId();
        this.topic = annotation.topic();
        this.expressionType = annotation.expressionType();
        this.expression = annotation.expression();
        this.messageModel = annotation.messageModel();
        this.consumeThreadNums = annotation.consumeThreadNums();
        this.consumeTimeout = annotation.consumeTimeout();
        this.consumeMessageBatchMaxSize = annotation.consumeMessageBatchMaxSize();
    }

    @Override
    public void start() {
        if (this.isRunning()) {
            throw new IllegalStateException("Container has already running. " + this.toString());
        }
        admin.start();
        this.setRunning(true);
        LOGGER.info("Running Container: {}", this.toString());
    }

    @Override
    public void stop() {
        if (this.isRunning()) {
            if (Objects.nonNull(admin)) {
                admin.shutdown();
            }
            setRunning(false);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }


    @Override
    public int getPhase() {
        // 返回Integer.MAX_VALUE保证容器会是第一个关闭的Bean，最后一个启动的Bean
        return Integer.MAX_VALUE;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initOnsConsumer();
    }

    @Override
    public void destroy() {
        this.setRunning(false);
        if (Objects.nonNull(admin)) {
            admin.shutdown();
        }
        LOGGER.info("Container has bean destroyed, {}", this.toString());
    }

    @Override
    public String toString() {
        return "DefaultOnsListenerContainer{" + "name='" + name + '\'' + ", accessKey='" + accessKey + '\''
                + ", secretKey='" + secretKey + '\'' + ", nameServerAddr='" + nameServerAddr + '\'' + ", consumeMode="
                + consumeMode + ", groupId='" + groupId + '\'' + ", topic='" + topic + '\'' + ", expressionType="
                + expressionType + ", expression='" + expression + '\'' + ", messageModel=" + messageModel
                + ", consumeThreadNums=" + consumeThreadNums + ", consumeTimeout=" + consumeTimeout
                + ", consumeMessageBatchMaxSize=" + consumeMessageBatchMaxSize + ", running=" + running + '}';
    }

    private void initOnsConsumer() {
        if (ConsumeMode.SERIAL.name().equals(getConsumeMode().name())) {
            initConsumerBean();
        } else if (ConsumeMode.BATCH.name().equals(getConsumeMode().name())) {
            initBatchConsumerBean();
        } else if (ConsumeMode.ORDERLY.name().equals(getConsumeMode().name())) {
            initOrderConsumerBean();
        } else {
            throw new IllegalArgumentException("Property 'consumeMode' was wrong.");
        }
    }

    private void initConsumerBean() {
        if (getMessageListener() == null) {
            throw new IllegalArgumentException("Property 'messageListener' is required");
        }
        ConsumerBean consumerBean = new ConsumerBean();
        // 设置属性
        consumerBean.setProperties(createProperties());
        // 创建订阅关系
        Map<Subscription, MessageListener> subscriptionMap = new HashMap<>(4);
        subscriptionMap.put(createSubscription(), getMessageListener());
        consumerBean.setSubscriptionTable(subscriptionMap);

        admin = consumerBean;

    }

    private void initBatchConsumerBean() {
        if (getBatchMessageListener() == null) {
            throw new IllegalArgumentException("Property 'batchMessageListener' is required");
        }
        BatchConsumerBean batchConsumerBean = new BatchConsumerBean();
        Properties properties = createProperties();
        properties.setProperty(PropertyKeyConst.ConsumeMessageBatchMaxSize, getConsumeMessageBatchMaxSize() + "");
        // 设置属性
        batchConsumerBean.setProperties(properties);
        // 创建订阅关系
        Map<Subscription, BatchMessageListener> subscriptionMap = new HashMap<>(4);
        subscriptionMap.put(createSubscription(), getBatchMessageListener());
        batchConsumerBean.setSubscriptionTable(subscriptionMap);

        admin = batchConsumerBean;

    }

    private void initOrderConsumerBean() {
        if (getMessageOrderListener() == null) {
            throw new IllegalArgumentException("Property 'messageOrderListener' is required");
        }
        OrderConsumerBean orderConsumerBean = new OrderConsumerBean();
        // 设置属性
        orderConsumerBean.setProperties(createProperties());
        // 创建订阅关系
        Map<Subscription, MessageOrderListener> subscriptionMap = new HashMap<>(4);
        subscriptionMap.put(createSubscription(), getMessageOrderListener());
        orderConsumerBean.setSubscriptionTable(subscriptionMap);

        admin = orderConsumerBean;

    }

    private Subscription createSubscription() {
        Subscription subscription = new Subscription();
        subscription.setTopic(getTopic());
        subscription.setType(getExpressionType().name());
        subscription.setExpression(getExpression());
        return subscription;
    }

    private Properties createProperties() {
        // 配置属性
        Properties properties = new Properties();
        // 组装ONS连接属性
        properties.setProperty(PropertyKeyConst.AccessKey, getAccessKey());
        properties.setProperty(PropertyKeyConst.SecretKey, getSecretKey());
        properties.setProperty(PropertyKeyConst.NAMESRV_ADDR, getNameServerAddr());
        // 设置消费者分组ID
        properties.setProperty(PropertyKeyConst.GROUP_ID, getGroupId());
        // 设置消费模式
        properties.setProperty(PropertyKeyConst.MessageModel, getMessageModel().getModel());
        // 设置消费者线程数 默认值为20
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, getConsumeThreadNums() + "");
        // 设置消费者超时时间，默认为15分钟
        properties.setProperty(PropertyKeyConst.ConsumeTimeout, getConsumeTimeout() + "");
        return properties;
    }


}
