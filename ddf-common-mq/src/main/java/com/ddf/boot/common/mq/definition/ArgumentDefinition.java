package com.ddf.boot.common.mq.definition;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 定义创建队列的各种参数类
 * <p>
 * https://www.cnblogs.com/xishuai/p/spring-boot-rabbitmq-delay-queue.html
 * <p>
 * 1. 延迟消费
 * 延迟消费是延迟队列最为常用的使用模式。如下图所示，生产者产生的消息首先会进入缓冲队列（图中红色队列）。
 * 通过RabbitMQ提供的TTL扩展，这些消息会被设置过期时间，也就是延迟消费的时间。等消息过期之后，
 * 这些消息会通过配置好的DLX转发到实际消费队列（图中蓝色队列），以此达到延迟消费的效果。
 * <p>
 * 2. 延迟重试
 * 消费者发现该消息处理出现了异常，比如是因为网络波动引起的异常。那么如果不等待一段时间，直接就重试的话，
 * 很可能会导致在这期间内一直无法成功，造成一定的资源浪费。那么我们可以将其先放在缓冲队列中（图中红色队列），
 * 等消息经过一段的延迟时间后再次进入实际消费队列中（图中蓝色队列），此时由于已经过了“较长”的时间了，
 * 异常的一些波动通常已经恢复，这些消息可以被正常地消费。
 * <p>
 * 3.
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
 *
 * @author dongfang.ding
 * @date 2019/8/1 10:19
 */
public class ArgumentDefinition {

    /**
     * 构建队列参数
     * 1. x-dead-letter-exchange 给当前队列声明一个死信交换器，与普通交换器定义方式并没有不同；只是消息消费失败之后，
     * 会根据交换器和路由键，将消息转发到与这里声明的交换器绑定的队列中
     * <p>
     * 2. x-dead-letter-routing-key 死信交换路由键
     * <p>
     * 3. x-message-ttl 队列的过期时间 如果要创建延时队列，其实是依赖于死信队列的；首先将一个队列声明成死信队列并且设置过期时间，
     *              发送的时候往这个队列里发送数据，但是不要消费，等消息过期后就会转发到与死信队列绑定的另外一个队列中，消费者
     *              要消费的是另外一个队列，千万不要搞错了！！
     *
     *              如果想要消息消费失败后，关闭重投功能；然后将消息转发到死信队列，那么可以结合ttl,完成延时重试的功能；步骤如下
     *              1. 首先是要发送和消费消息的原始队列，然后声明成死信队列，将交换器路由到另外一个死信队列
     *              2. 另外一个死信队列设置ttl，当原始队列拒绝消息后，消息转发到该死信队列，该死信队列路由到另外一个正常的消息队列；
     *                 该死信队列不要设置消费者，等待ttl，然后消息路由到该死信队列交换器绑定的队列中
     *              3. 建立消费者，监听最后一个队列，执行消费；
     *
     * 消息进入死信队列的情况
     *
     * 1. 消息被拒绝。通过调用basic.reject或者basic.nack并且设置的requeue参数为false。
     * 2. 消息因为设置了TTL而过期。
     * 3. 消息进入了一条已经达到最大长度的队列。
     *
     * @param redirectTo
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author dongfang.ding
     * @date 2019/12/12 0012 16:23
     **/
    public static Map<String, Object> deadLetterRedirectTo(QueueBuilder.QueueDefinition redirectTo, long ttl) {
        Map<String, Object> argumentMap = new HashMap<>(3);
        if (StringUtils.isNotBlank(redirectTo.getExchangeName())) {
            argumentMap.put("x-dead-letter-exchange", redirectTo.getExchangeName());
        }
        if (StringUtils.isNotBlank(redirectTo.getRouteKey())) {
            argumentMap.put("x-dead-letter-routing-key", redirectTo.getRouteKey());
        }
        if (ttl != 0) {
            argumentMap.put("x-message-ttl", ttl);
        }
        return argumentMap;
    }

    /**
     * 只包含死信队列的定义参数
     *
     * @param redirectTo
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author dongfang.ding
     * @date 2019/12/16 0016 13:30
     **/
    public static Map<String, Object> deadLetterRedirectTo(QueueBuilder.QueueDefinition redirectTo) {
        return deadLetterRedirectTo(redirectTo, 0);
    }
}
