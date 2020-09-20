package com.ddf.boot.common.mq.config;

import com.ddf.boot.common.mq.definition.MqMessageWrapper;
import com.ddf.boot.common.mq.definition.QueueBuilder;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * 自定义封装的一些mq方法全局属性
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
 * @date 2019/12/11 0011 10:30
 */
@Data
@Component
@ConfigurationProperties(prefix = "customs.mq-message-properties")
public class MqMessageProperties/* implements InitializingBean*/ {

    /**
     * 最大重投次数
     *
     * @see com.ddf.boot.common.mq.helper.RabbitTemplateHelper#nackAndRequeue(Channel, Message, QueueBuilder.QueueDefinition, MqMessageWrapper, Consumer)
     */
    private int maxRequeueTimes = 5;

    /**
     * 针对发送事件异步消息落库实现的一个内存队列的最大值
     *
     * @see com.ddf.boot.common.mq.listener.DefaultMqEventListener#MESSAGE_QUEUE
     */
    private int messageQueueSize = 10000;

    /**
     * 落库监听日志的实现类的beanName
     * 默认使用Mongo落库，可切换为数据库
     * @see com.ddf.boot.common.mq.persistence.LogMqPersistenceProcessor
     */
    private String logMqPersistenceProcessorBeanName = "mongoLogMqPersistenceProcessor";

//    @Override
//    public void afterPropertiesSet() throws Exception {
//        Object bean = SpringContextHolder.getBean(logMqPersistenceProcessorBeanName);
//        if (!(bean instanceof LogMqPersistenceProcessor)) {
//            throw new NoSuchBeanDefinitionException(logMqPersistenceProcessorBeanName);
//        }
//    }
}
