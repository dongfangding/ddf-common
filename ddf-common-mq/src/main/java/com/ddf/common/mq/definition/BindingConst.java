package com.ddf.common.mq.definition;

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
    public static final String PREFIX = "yaokepay.";

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
        public static final String TEST_QUEUE = PREFIX + "test.queue";
        /**
         * 网关日志队列名
         */
        public static final String GATEWAY_LOG = PREFIX + "gateway.log.queue";
        /**
         * 短信解析内容队列名
         */
        public static final String BANK_SMS_PARSE = PREFIX + "bank.sms.parse.queue";
        /**
         * 收款订单通知二维码加解锁队列
         */
        public static final String ORDER_NOTIFY_QRCODE_USER_STATUS = PREFIX + "order.notify.qrcode.user.status.queue";
        /**
         * 中介充值订单通知二维码加解锁队列
         */
        public static final String ORDER_NOTIFY_QRCODE_MEDIATOR_STATUS = PREFIX + "order.notify.qrcode.mediator.status.queue";
        /**
         * 收款订单通知账户冻结解冻队列
         */
        public static final String ORDER_NOTIFY_ACCOUNT_FROZEN_OR_UNFROZEN = PREFIX + "order.notify.account.frozen.or.unfrozen.queue";

        /**
         * 账户通知订单服务修改订单状态
         */
        public static final String ACCOUNT_NOTIFY_ORDER_UPDATE_ORDER_STATUS = PREFIX + "account.notify.order.update.order.status.queue";
    }

    /**
     * 路由键名称
     */
    public class RouteKey {
        public static final String TEST_ROUTE_KEY = "test.route.key";
        /**
         * 网关日志路由键
         */
        public static final String GATEWAY_LOG = "gateway.log";

        /**
         * 短信解析内容路由键
         */
        public static final String BANK_SMS_PARSE = "bank.sms.parse";
        /**
         * 收款订单通知二维码加解锁路由键
         */
        public static final String ORDER_NOTIFY_QRCODE_USER_STATUS = "order.notify.qrcode.user.status";
        /**
         * 中介充值订单通知二维码加解锁路由键
         */
        public static final String ORDER_NOTIFY_QRCODE_MEDIATOR_STATUS = "order.notify.qrcode.mediator.status";
        /**
         * 收款订单通知账户冻结解冻路由键
         */
        public static final String ORDER_NOTIFY_ACCOUNT_FROZEN_OR_UNFROZEN = "order.notify.account.frozen.or.unfrozen";

        /**
         * 账户通知订单服务修改订单状态
         */
        public static final String ACCOUNT_NOTIFY_ORDER_UPDATE_ORDER_STATUS = "account.notify.order.update.order.status";
    }

}
