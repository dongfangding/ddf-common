package com.ddf.boot.common.mq.Initialize;

import com.ddf.boot.common.mq.definition.QueueBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 根据预定义的队列/交换器/路由键信息声明队列
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
 * @date 2019/7/31 17:14
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class AmqpDeclareBean implements InitializingBean {

    @Autowired
    private AmqpAdmin amqpAdmin;

    /**
     * 根据预定义的队列配置自动创建队列和交换器以及绑定他们的关系
     * 
     * @author dongfang.ding
     * @date 2019/7/31 19:20 
     */
    private void buildQueueDefinition() {
        log.info("start buildQueueDefinition>>>>>>>>>>>>>");
        QueueBuilder.QueueDefinition[] values = QueueBuilder.QueueDefinition.values();
        if (values.length > 0) {
            for (QueueBuilder.QueueDefinition value : values) {
                Queue queue = new Queue(value.getQueueName(), true, false, false, value.getQueueArguments());
                Exchange exchange;
                if (QueueBuilder.ExchangeType.DIRECT.equals(value.getExchangeType())) {
                    exchange = new DirectExchange(value.getExchangeName(), true, false, value.getExchangeArguments());
                } else if (QueueBuilder.ExchangeType.FANOUT.equals(value.getExchangeType())) {
                    exchange = new FanoutExchange(value.getExchangeName(), true, false, value.getExchangeArguments());
                } else if (QueueBuilder.ExchangeType.TOPIC.equals(value.getExchangeType())) {
                    exchange = new TopicExchange(value.getExchangeName(), true, false, value.getExchangeArguments());
                } else {
                    throw new RuntimeException("暂不支持的交换器类型!");
                }
                amqpAdmin.declareQueue(queue);
                amqpAdmin.declareExchange(exchange);
                if (value.getBindingArguments() != null && !value.getBindingArguments().isEmpty()) {
                    amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(value.getRouteKey()).and(value.getBindingArguments()));
                } else {
                    amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(value.getRouteKey()).noargs());
                }
            }
        }
    }

    /**
     * Invoked by the containing {@code BeanFactory} after it has set all bean properties
     * and satisfied {@link BeanFactoryAware}, {@code ApplicationContextAware} etc.
     * <p>This method allows the bean instance to perform validation of its overall
     * configuration and final initialization when all bean properties have been set.
     *
     * @throws Exception in the event of misconfiguration (such as failure to set an
     *                   essential property) or if initialization fails for any other reason
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        buildQueueDefinition();
    }
}
