package com.ddf.boot.common.mq.entity;

import com.ddf.boot.common.core.model.BaseDomain;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 消息发送和消费的记录实体
 * <p>
 * 消息在不同的队列中传输，但通过唯一标识符messageId来标识；无论在几个队列中传输和发送几次，最终落到数据库中永远是以最后一次
 * 产生的结果来存储的
 * 1. 消息在正常队列中发送，发送成功还未消费时状态未SEND_SUCCESS, 发送失败为SEND_FAILURE, 当发送成功时，消费监听执行后，消费成功
 * 状态变更为CONSUMER_SUCCESS, 消费失败为CONSUMER_FAILURE
 * <p>
 * 2. 在第一种情况下，如果消费失败，并开启了重投机制，该条消息依然保证在数据库中的唯一记录，只是会以最新的数据为准，所以重投次数，队列，
 * 交换器相关信息都有可能会发生变化
 * <p>
 * 3. 消息在死信队列中消费失败，消息被转发， 消息的队列会由原始的死信队列变更为用来接收死信队列消息的另外一个队列相关信息，并执行新的消费等逻辑
 * 消息还是一条，就像不曾在死信队列中存在过一般
 * <p>
 * 4. 延迟队列，消息最初发往死信延迟队列，发送成功后，因为不会对其进行消费，因此该条消息没有消费相关信息。直到消息过期，重新被转发到另外一个
 * 队列，该消息与发送相关的队列信息被更新掉，然后根据消费结果更新消费信息，同样看起来，该条消息就像不曾在最初的延迟队列中存在一般。
 *
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
 *
 * @author dongfang.ding
 * @date 2019/12/20 0020 13:45
 */
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Document
public class LogMqListener extends BaseDomain implements Serializable {

    @Indexed(unique = true)
    private String messageId;

    /**
     * 消息的创建人
     */
    private String creator;

    /**
     * 消息当前重投次数
     */
    private Integer requeueTimes;

    /**
     * 当前消息json串
     */
    private String messageJson;

    /**
     * 消息最新发送时间
     */
    private Long sendTimestamp;

    /**
     * 消息最新消费时间
     */
    private Long consumerTimestamp;

    /**
     * 消息事件发生的时间戳，实际上这个时间理论上就是要么就是发送时间要么就是消费时间
     */
    private Long eventTimestamp;

    /**
     * 消息事件
     */
    private String event;

    /**
     * 交换器名称
     */
    @Indexed
    private String exchangeName;

    /**
     * 交换器类型
     */
    private String exchangeType;

    /**
     * 路由键名称
     */
    private String routeKey;

    /**
     * 预期队列名称（根据定义中获取的队列名）
     */
    private String targetQueue;

    /**
     * 实际消费队列名称（根据@RabbitListener获取）
     */
    @Indexed
    private String actualQueue;

    /**
     * 消息消费容器工厂beanName
     */
    private String containerFactory;

    /**
     * 处理线程
     */
    private String currentThreadName;

    /**
     * 失败消息
     */
    private String errorMessage;

    /**
     * 失败错误堆栈消息
     */
    private String errorStack;
}
