package com.ddf.boot.common.rocketmq.dto;

/**
 * <p>用来存储RocketMQ使用期间需要定义的常量</p >
 * <p>
 * <p>
 * 针对定义来说，使用枚举可能更好， 但是针对注解上的引用来说，使用常量可以直接方便引用
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/11/19 19:16
 */
public interface RocketMQConstantsDemo {


    /**
     * 消息topic
     */
    interface Topic {

        /**
         * 演示Topic
         */
        String DEMO = "demo";


    }


    /**
     * 应保持Topic的通用性，使用tags来筛选数据, 这可以不用建立太多的topic
     * <p>
     * https://github.com/apache/rocketmq/blob/master/docs/cn/best_practice.md#1--tags%E7%9A%84%E4%BD%BF%E7%94%A8
     */
    interface Tags {

        String STRING = "string";
    }


    /**
     * 消费组
     */
    interface ConsumerGroup {

        String DEMO_STRING_CONSUMER_GROUP = "demo_consumer_group";

    }



}
