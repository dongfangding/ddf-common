package com.ddf.boot.common.mq.definition;

import java.util.HashMap;
import java.util.Map;

/**
 * 定义创建队列的各种参数类
 *
 * https://www.cnblogs.com/xishuai/p/spring-boot-rabbitmq-delay-queue.html
 *
 * 1. 延迟消费
 *      延迟消费是延迟队列最为常用的使用模式。如下图所示，生产者产生的消息首先会进入缓冲队列（图中红色队列）。
 *      通过RabbitMQ提供的TTL扩展，这些消息会被设置过期时间，也就是延迟消费的时间。等消息过期之后，
 *      这些消息会通过配置好的DLX转发到实际消费队列（图中蓝色队列），以此达到延迟消费的效果。
 *
 * 2. 延迟重试
 *      消费者发现该消息处理出现了异常，比如是因为网络波动引起的异常。那么如果不等待一段时间，直接就重试的话，
 *      很可能会导致在这期间内一直无法成功，造成一定的资源浪费。那么我们可以将其先放在缓冲队列中（图中红色队列），
 *      等消息经过一段的延迟时间后再次进入实际消费队列中（图中蓝色队列），此时由于已经过了“较长”的时间了，
 *      异常的一些波动通常已经恢复，这些消息可以被正常地消费。
 *
 * 3.
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
 * @date 2019/8/1 10:19
 */
public class ArgumentDefinition {



    /**
     * Time-To-Live Extensions
     * RabbitMQ允许我们为消息或者队列设置TTL（time to live），也就是过期时间。TTL表明了一条消息可在队列中存活的最大时间，单位为毫秒。
     * 也就是说，当某条消息被设置了TTL或者当某条消息进入了设置了TTL的队列时，这条消息会在经过TTL秒后“死亡”，成为Dead Letter。
     * 如果既配置了消息的TTL，又配置了队列的TTL，那么较小的那个值会被取用。
     * 
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author dongfang.ding
     * @date 2019/12/12 0012 13:32
     **/
    public static Map<String, Object> deadArgs() {
        Map<String, Object> argumentMap = new HashMap<>(2);
        argumentMap.put("x-dead-letter-exchange", BindingConst.ExchangeName.DEFAULT);
        argumentMap.put("x-dead-letter-routing-key", BindingConst.RouteKey.USER_LOGIN_HISTORY_KEY);
        return argumentMap;
    }
    
    
    /**
     * 被设置了TTL的消息在过期后会成为Dead Letter。其实在RabbitMQ中，一共有三种消息的“死亡”形式：
     *
     * 1. 消息被拒绝。通过调用basic.reject或者basic.nack并且设置的requeue参数为false。
     * 2. 消息因为设置了TTL而过期。
     * 3. 消息进入了一条已经达到最大长度的队列。
     *
     * Dead Letter Exchange
     * @param null
     * @return null
     * @author dongfang.ding
     * @date 2019/12/12 0012 13:32
     **/

}
