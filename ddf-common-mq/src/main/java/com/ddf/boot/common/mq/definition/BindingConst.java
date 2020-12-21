package com.ddf.boot.common.mq.definition;

import com.ddf.boot.common.mq.config.MqAutoConfiguration;

/**
 * 队列、交换器、路由键的常量类
 * <p>
 * <p>
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
 */
public class BindingConst {

    /***
     * 所有定义的交换器、队列的共同前缀名
     */
    public static final String PREFIX = "com.ddf.boot.common.";

    /**
     * 死信队列的前缀
     */
    public static final String DEAD_LETTER_PREFIX = PREFIX + "dead.letter.";

    /**
     * 延时队列的前缀
     */
    public static final String TTL_PREFIX = PREFIX + "ttl.";

    /**
     * 消费端消息确认模式 Bean Name>>> 单消费者AutoAck
     *
     * @see MqAutoConfiguration#singleAutoAck(org.springframework.amqp.rabbit.connection.CachingConnectionFactory)
     */
    public static final String ACK_MODE_SINGLE_AUTO_ACK = "singleAutoAck";

    /**
     * 消费端消息确认模式 Bean Name>>> 多消费者AutoAck
     *
     * @see MqAutoConfiguration#concurrentAutoAck(org.springframework.amqp.rabbit.connection.CachingConnectionFactory)
     */
    public static final String ACK_MODE_CONCURRENT_AUTO_ACK = "concurrentAutoAck";

    /**
     * 消费端消息确认模式 Bean Name>>> 单消费者手动ack
     *
     * @see MqAutoConfiguration#singleManualAck(org.springframework.amqp.rabbit.connection.CachingConnectionFactory)
     */
    public static final String ACK_MODE_SINGLE_MANUAL_ACK = "singleManualAck";

    /**
     * 消费端消息确认模式 Bean Name>>> 多消费者手动ack
     *
     * @see MqAutoConfiguration#concurrentManualAck(org.springframework.amqp.rabbit.connection.CachingConnectionFactory)
     */
    public static final String ACK_MODE_CONCURRENT_MANUAL_ACK = "concurrentManualAck";

    /**
     * 消费端消息确认模式 Bean Name>>> 单消费者none ack
     *
     * @see MqAutoConfiguration#noneAck(org.springframework.amqp.rabbit.connection.CachingConnectionFactory)
     */
    public static final String ACK_MODE_NONE_ACK = "noneAck";

    /**
     * 消费端消息确认模式 Bean Name>>> 多消费者none ack
     *
     * @see MqAutoConfiguration#concurrentNoneAck(org.springframework.amqp.rabbit.connection.CachingConnectionFactory)
     */
    public static final String ACK_MODE_CONCURRENT_NONE_ACK = "concurrentNoneAck";


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
         * 默认的DIRECT类型的死信交换器名称
         */
        public static final String DEFAULT = DEAD_LETTER_PREFIX + "direct.default";

    }


    /**
     * 队列名称
     */
    public class QueueName {
        // -----------------------------------------------------------------------------------------
        /**
         * 测试正常队列
         */
        public static final String TEST_NORMAL_QUEUE = PREFIX + "test.normal.queue";


        // ---------------------------------------------------------------------------------------
        /**
         * 测试基本死信队列, 该队列调用nack或reject方法，并且requeue设置为false，之后消息会根据死信路由转发到另外一个队列
         */
        public static final String TEST_DEAD_LETTER_QUEUE = DEAD_LETTER_PREFIX + "test.dead.letter.queue";

        /**
         * 测试基本死信队列的接收队列， 上述死信队列出现死信数据后，消息将被转发到该队列
         */
        public static final String TEST_DEAD_LETTER_RECEIVE_QUEUE =
                DEAD_LETTER_PREFIX + "test.dead.letter.receive.queue";

        // ----------------------------------------------------------------------------------------

        /**
         * 测试延时队列，延时队列依赖与死信队列
         */
        public static final String TEST_TTL_QUEUE = TTL_PREFIX + "test.ttl.queue";

        /**
         * 测试延时队列，延时队列依赖与死信队列
         */
        public static final String TEST_TTL_RECEIVE_QUEUE = TTL_PREFIX + "test.ttl.receive.queue";


        // ----------------------------------------------------------------------------------------

        /**
         * 用户登录日志
         */
        public static final String USER_LOGIN_HISTORY_QUEUE = PREFIX + "user.login.token.queue";


        /**
         * 设备指令运行状态监控数据持久化
         */
        public static final String DEVICE_CMD_RUNNING_STATE_PERSISTENCE =
                PREFIX + "device.cmd.running.state.persistence";

    }


    /**
     * 路由键名称
     */
    public class RouteKey {

        /**
         * 测试正常队列路由键
         */
        public static final String TEST_NORMAL_KEY = PREFIX + "test.normal.queue";

        // --------------------------------------------------------------------------------------------

        /**
         * 测试基本死信队列路由键
         */
        public static final String TEST_DEAD_LETTER_KEY = DEAD_LETTER_PREFIX + "test.dead.letter.key";

        /**
         * 测试基本死信队列的接收队列路由键
         */
        public static final String TEST_DEAD_LETTER_RECEIVE_KEY = DEAD_LETTER_PREFIX + "test.dead.letter.receive.queue";

        // ------------------------------------------------------------------------------------------------

        /**
         * 测试延时队列路由键
         */
        public static final String TEST_TTL_KEY = TTL_PREFIX + "test.ttl.key";
        /**
         * 测试延时队列接收队列的路由键
         */
        public static final String TEST_TTL_RECEIVE_KEY = TTL_PREFIX + "test.ttl.receive_key";

        // ----------------------------------------------------------------------------------------


        /**
         *
         */
        public static final String USER_LOGIN_HISTORY_KEY = "user.login.token.key";

        /**
         * 设备指令运行状态监控数据持久化
         */
        public static final String DEVICE_CMD_RUNNING_STATE_PERSISTENCE_KEY =
                PREFIX + "device.cmd.running.state.persistence.key";

    }

}
