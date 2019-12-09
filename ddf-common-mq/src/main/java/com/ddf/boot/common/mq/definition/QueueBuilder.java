package com.ddf.boot.common.mq.definition;

import lombok.Getter;

import java.util.Map;

/**
 * 队列配置生成类模板
 *
 * @author dongfang.ding
 * @date 2019/7/31 16:32
 */
public class QueueBuilder {

    /**
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
         * 自动ack队列绑定
         */
        TEST_AUTO_ACK(BindingConst.QueueName.TEST_AUTO_ACK_QUEUE, BindingConst.ExchangeName.DIRECT, ExchangeType.DIRECT, BindingConst.RouteKey.TEST_AUTO_ACK_KEY),

        /**
         * 用户token
         */
        TEST_QUEUE(BindingConst.QueueName.USER_LOGIN_TOKEN_QUEUE, BindingConst.ExchangeName.DIRECT, ExchangeType.DIRECT, BindingConst.RouteKey.USER_LOGIN_TOKEN_KEY),



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
            checkPrefix(this.queueName);
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
            checkPrefix(this.queueName);
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

    /**
     * 交换器类型类
     */
    public enum ExchangeType {
        /**
         * 交换器类型
         */
        DIRECT, TOPIC, FANOUT

    }

}
