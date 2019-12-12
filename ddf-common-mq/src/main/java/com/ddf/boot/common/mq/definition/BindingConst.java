package com.ddf.boot.common.mq.definition;

/**
 * 队列、交换器、路由键的常量类
 *
 *
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
 *
 */
public class BindingConst {

    /***
     * 所有定义的交换器、队列的共同前缀名
     */
    public static final String PREFIX = "com.ddf.boot.common.";

    public static final String DEAD_LETTER_PREFIX = PREFIX + "dead.letter.";

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

        /**
         * 默认的死信交换器
         */
        public static final String DEFAULT = DEAD_LETTER_PREFIX + "default";

    }

    /**
     * 队列名称
     */
    public class QueueName {

        /**
         * 用户登录日志
         */
        public static final String USER_LOGIN_HISTORY_QUEUE = PREFIX + "user.login.token.queue";

        /**
         * 用户登录日志的死信消费队列
         */
        public static final String DEAD_LETTER_USER_LOGIN_HISTORY_QUEUE = DEAD_LETTER_PREFIX + "user.login.token.queue";
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
