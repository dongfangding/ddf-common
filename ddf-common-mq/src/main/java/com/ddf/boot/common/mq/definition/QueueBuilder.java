package com.ddf.boot.common.mq.definition;

import lombok.Getter;

import java.util.Map;

/**
 * 队列配置生成类模板
 * 该方式是在容器初始化的时候调用预定义的模板来进行队列与交换器和路由的声明以及绑定；
 * 目的是为了集中管理队列相关配置信息；
 * 但是带来的劣势是，如果使用这种方式，那么在项目运行过程中，如果有人把队列删掉了，那么与之对应的
 * 相关消费就会出现问题；
 *
 * 除非消费端使用auth declare（在消费配置上使用declare能够在队列不存在的时候创建队列）功能，但是那种方式与该类的初衷是相悖的。
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
 * @author dongfang.ding
 * @date 2019/7/31 16:32
 */
public class QueueBuilder {

    /**
     * 交换器类型类
     */
    public enum ExchangeType {
        /**
         * 交换器类型
         */
        DIRECT, TOPIC, FANOUT

    }

    /**
     *
     * FIXME 如何做到让使用方可以在外部自定义自己的队列而不用修改源码？
     * 如果时普通的类可以通过接口，但现在是个枚举，不能让使用方还写那么多重复的已定义的属性和构造方法
     *
     * 队列的配置
     * 暂不支持复杂的队列配置
     * <p>
     * Queue的属性：
     * durable: true
     * exclusive: false
     * autoDelete: false
     * <p>
     * Exchange的属性
     * durable: true
     * autoDelete: false
     */
    @Getter
    public enum QueueDefinition {

        /**
         * 正常队列， 发送和消费都针对这个队列操作即可
         */
        TEST_NORMAL_QUEUE(BindingConst.QueueName.TEST_NORMAL_QUEUE, BindingConst.ExchangeName.DIRECT, ExchangeType.DIRECT,
                BindingConst.RouteKey.TEST_NORMAL_KEY),

        // ------------------------------------------------------------------------------------------------------

        /**
         * 测试基本死信队列的接收队列，该队列是一个正常队列，上述死信队列出现死信数据后，消息将被转发到该队列,后续消息消费该队列
         */
        TEST_DEAD_LETTER_RECEIVE_QUEUE(BindingConst.QueueName.TEST_DEAD_LETTER_RECEIVE_QUEUE, BindingConst.ExchangeName.DIRECT,
                ExchangeType.DIRECT, BindingConst.RouteKey.TEST_DEAD_LETTER_RECEIVE_KEY),


        /**
         * 测试基本死信队列
         */
        TEST_DEAD_LETTER_QUEUE(BindingConst.QueueName.TEST_DEAD_LETTER_QUEUE, BindingConst.ExchangeName.DIRECT, ExchangeType.DIRECT,
                          BindingConst.RouteKey.TEST_DEAD_LETTER_KEY, ArgumentDefinition.deadLetterRedirectTo(TEST_DEAD_LETTER_RECEIVE_QUEUE)),


        // ------------------------------------------------------------------------------------------------------



        /**
         * 测试延时队列的接收队列，延时队列依赖与死信队列
         */
        TEST_TTL_RECEIVE_QUEUE(BindingConst.QueueName.TEST_TTL_RECEIVE_QUEUE, BindingConst.ExchangeName.DIRECT,
                ExchangeType.DIRECT, BindingConst.RouteKey.TEST_TTL_RECEIVE_KEY),


        /**
         * 测试延时队列，其实是一个死信队列，然后设置ttl，不消费该队列，等待消息过期，然后转发到另外一个接收队列上，从而实现延时队列
         */
        TEST_TTL_QUEUE(BindingConst.QueueName.TEST_TTL_QUEUE, BindingConst.ExchangeName.DIRECT, ExchangeType.DIRECT,
                BindingConst.RouteKey.TEST_TTL_KEY, ArgumentDefinition.deadLetterRedirectTo(TEST_TTL_RECEIVE_QUEUE, 10000)),


        // ------------------------------------------------------------------------------------------------------


        /**
         * 用户登录日志
         */
        USER_LOGIN_HISTORY_QUEUE(BindingConst.QueueName.USER_LOGIN_HISTORY_QUEUE, BindingConst.ExchangeName.DIRECT,
                ExchangeType.DIRECT, BindingConst.RouteKey.USER_LOGIN_HISTORY_KEY),

        /**
         * 设备指令运行状态监控数据持久化
         */
        DEVICE_CMD_RUNNING_STATE_PERSISTENCE_QUEUE(BindingConst.QueueName.DEVICE_CMD_RUNNING_STATE_PERSISTENCE,
                BindingConst.ExchangeName.DIRECT, ExchangeType.DIRECT,
                BindingConst.RouteKey.DEVICE_CMD_RUNNING_STATE_PERSISTENCE_KEY),

        ;

        /**
         * 队列名称
         */
        private String queueName;
        /**
         * 交换器
         */
        private String exchangeName;
        /**
         * 交换器类型
         */
        private ExchangeType exchangeType;
        /**
         * 路由键
         */
        private String routeKey;
        /**
         * 队列的附加参数
         */
        private Map<String, Object> queueArguments;
        /**
         * 交换器创建时的附加参数
         */
        private Map<String, Object> exchangeArguments;
        /**
         * 创建Binding时附加参数
         */
        private Map<String, Object> bindingArguments;

        /**
         * 默认的基础队列创建定义
         *
         * @param queueName
         * @param exchangeName
         * @param exchangeType
         * @param routeKey
         */
        QueueDefinition(String queueName, String exchangeName, ExchangeType exchangeType, String routeKey) {
            this.queueName = queueName;
            this.exchangeName = exchangeName;
            this.exchangeType = exchangeType;
            this.routeKey = routeKey;
            checkPrefix(queueName);
        }


        /**
         * 可以给队列、交换器、绑定类附加参数
         *
         * @param queueName
         * @param exchangeName
         * @param exchangeType
         * @param routeKey
         * @param queueArgument
         */
        QueueDefinition(String queueName, String exchangeName, ExchangeType exchangeType, String routeKey,
                        Map<String, Object> queueArgument) {
            this.queueName = queueName;
            this.exchangeName = exchangeName;
            this.exchangeType = exchangeType;
            this.routeKey = routeKey;
            this.queueArguments = queueArgument;
            checkPrefix(queueName);
        }


        /**
         * 可以给队列、交换器、绑定类附加参数
         *
         * @param queueName
         * @param exchangeName
         * @param exchangeType
         * @param routeKey
         * @param queueArguments
         * @param exchangeArguments
         */
        QueueDefinition(String queueName, String exchangeName, ExchangeType exchangeType, String routeKey,
                        Map<String, Object> queueArguments, Map<String, Object> exchangeArguments) {
            this.queueName = queueName;
            this.exchangeName = exchangeName;
            this.exchangeType = exchangeType;
            this.routeKey = routeKey;
            this.queueArguments = queueArguments;
            this.exchangeArguments = exchangeArguments;
            checkPrefix(queueName);
        }

        /**
         * 可以给队列、交换器、绑定类附加参数
         *
         * @param queueName
         * @param exchangeName
         * @param exchangeType
         * @param routeKey
         * @param queueArguments
         * @param exchangeArguments
         * @param bindingArguments
         */
        QueueDefinition(String queueName, String exchangeName, ExchangeType exchangeType, String routeKey,
                        Map<String, Object> queueArguments, Map<String, Object> exchangeArguments,
                        Map<String, Object> bindingArguments) {
            this.queueName = queueName;
            this.exchangeName = exchangeName;
            this.exchangeType = exchangeType;
            this.routeKey = routeKey;
            this.queueArguments = queueArguments;
            this.exchangeArguments = exchangeArguments;
            this.bindingArguments = bindingArguments;
            checkPrefix(queueName);
        }


        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        public String getExchangeName() {
            return exchangeName;
        }

        public void setExchangeName(String exchangeName) {
            this.exchangeName = exchangeName;
        }

        public ExchangeType getExchangeType() {
            return exchangeType;
        }

        public void setExchangeType(ExchangeType exchangeType) {
            this.exchangeType = exchangeType;
        }

        public String getRouteKey() {
            return routeKey;
        }

        public void setRouteKey(String routeKey) {
            this.routeKey = routeKey;
        }

        public Map<String, Object> getQueueArguments() {
            return queueArguments;
        }

        public void setQueueArguments(Map<String, Object> queueArguments) {
            this.queueArguments = queueArguments;
        }

        public Map<String, Object> getExchangeArguments() {
            return exchangeArguments;
        }

        public void setExchangeArguments(Map<String, Object> exchangeArguments) {
            this.exchangeArguments = exchangeArguments;
        }

        public Map<String, Object> getBindingArguments() {
            return bindingArguments;
        }

        public void setBindingArguments(Map<String, Object> bindingArguments) {
            this.bindingArguments = bindingArguments;
        }
    }


    /**
     * 校验命名合法性
     *
     * @param source
     */
    private static void checkPrefix(String... source) {
        String msg = "格式必须以[" + BindingConst.PREFIX + "]开头";
        if (source.length == 0) {
            throw new RuntimeException(msg);
        }
        for (String s : source) {
            if (!s.startsWith(BindingConst.PREFIX)) {
                throw new RuntimeException(msg);
            }
        }
    }

}
