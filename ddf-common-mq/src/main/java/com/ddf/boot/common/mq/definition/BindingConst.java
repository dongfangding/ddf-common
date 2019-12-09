package com.ddf.boot.common.mq.definition;

/**
 * 队列、交换器、路由键的常量类
 *
 * @author dongfang.ding
 * @date 2019/8/5 13:35
 */
public class BindingConst {

    /***
     * 所有定义的交换器、队列的共同前缀名
     */
    public static final String PREFIX = "com.ddf.boot.common.";

    /**
     * 交换器名称
     */
    public class ExchangeName {
        public static final String DIRECT = "amqp.direct";
        public static final String FANOUT = "amqp.fanout";
        public static final String TOPIC = "amqp.topic";
    }

    /**
     * 队列名称
     */
    public class QueueName {

        /**
         * 测试自动ack的队列
         */
        public static final String TEST_AUTO_ACK_QUEUE = PREFIX + "test.auto.ack.queue";


        public static final String USER_LOGIN_TOKEN_QUEUE = PREFIX + "user.login.token.queue";
    }

    /**
     * 路由键名称
     */
    public class RouteKey {

        /**
         * 测试自动ack的路由键
         */
        public static final String TEST_AUTO_ACK_KEY = PREFIX + "test.auto.ack.key";

        public static final String USER_LOGIN_TOKEN_KEY = PREFIX + "user.login.token.key";
    }

}
