package com.ddf.boot.common.mq.definition;

/**
 * 队列、交换器、路由键的常量类
 *

company
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

        /**
         * 默认的交换器名称
         */
        public static final String DIRECT = "amqp.direct";

        /**
         * 默认的交换器名称
         */
        public static final String FANOUT = "amqp.fanout";

        /**
         * 默认的交换器名称
         */
        public static final String TOPIC = "amqp.topic";

    }

    /**
     * 队列名称
     */
    public class QueueName {

        /**
         * 用户登录日志
         */
        public static final String USER_LOGIN_HISTORY_QUEUE = PREFIX + "user.login.token.queue";
    }

    /**
     * 路由键名称
     */
    public class RouteKey {

        /**
         * 收款订单路由
         */
        public static final String USER_LOGIN_HISTORY_KEY = "user.login.token.key";

    }

}
