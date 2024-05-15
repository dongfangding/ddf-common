package com.ddf.common.ons.consumer.batch;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.batch.BatchMessageListener;
import com.aliyun.openservices.ons.api.bean.BatchConsumerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.ddf.common.ons.consumer.OnsConsumer;
import com.ddf.common.ons.properties.OnsProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ONS批量消息抽象消费者
 *
 * @author snowball
 * @date 2021/8/26 16:45
 **/
public abstract class AbstractBatchOnsConsumer implements OnsConsumer {

    private final String CONSUME_THREAD_NUMS = "20";

    @Autowired
    protected OnsProperties onsConfiguration;

    /**
     * 创建消费者Bean
     * @return
     */
    protected BatchConsumerBean createBatchConsumerBean() {
        BatchConsumerBean consumerBean = new BatchConsumerBean();
        // 配置文件
        Properties properties = onsConfiguration.getOnsProperties();
        properties.setProperty(PropertyKeyConst.GROUP_ID, getGroupId());
        // 设置消费者线程数 默认值为20
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, getConsumeThreadNums());
        consumerBean.setProperties(properties);
        // 订阅关系
        Map<Subscription, BatchMessageListener> subscriptionTable = new HashMap<>();

        Subscription subscription = new Subscription();
        subscription.setTopic(getTopic());
        subscription.setExpression(getExpression());
        subscriptionTable.put(subscription, getMessageListener());

        consumerBean.setSubscriptionTable(subscriptionTable);

        return consumerBean;
    }


    /**
     * 获取并发消费者数量
     * @return
     */
    @Override
    public String getConsumeThreadNums() {
        return CONSUME_THREAD_NUMS;
    }

    /**
     * 获取消息监听器
     * @return
     */
    protected abstract BatchMessageListener getMessageListener();

}
