package com.ddf.boot.common.mq.entity;

import com.ddf.boot.common.entity.BaseDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 *
 * 消息发送和消费的记录实体
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
@ApiModel("消息发送和消费的记录实体")
public class LogMqListener extends BaseDomain implements Serializable {

    @ApiModelProperty("消息的唯一标识符")
    private String messageId;

    @ApiModelProperty("消息的创建人")
    private String creator;

    @ApiModelProperty("消息当前重投次数")
    private Integer requeueTimes;

    @ApiModelProperty("当前消息json串")
    private String messageJson;

    @ApiModelProperty("消息事件")
    private String event;

    @ApiModelProperty("消息事件发生的时间戳")
    private Long timestamp;

    @ApiModelProperty("交换器名称")
    private String exchangeName;

    @ApiModelProperty("交换器类型")
    private String exchangeType;

    @ApiModelProperty("路由键名称")
    private String routeKey;

    @ApiModelProperty("预期队列名称（根据定义中获取的队列名）")
    private String targetQueue;

    @ApiModelProperty("实际消费队列名称（根据@RabbitListener获取）")
    private String actualQueue;

    @ApiModelProperty("消息消费容器工厂beanName")
    private String containerFactory;

    @ApiModelProperty("处理线程")
    private String currentThreadName;

    @ApiModelProperty("失败消息")
    private String errorMessage;

    @ApiModelProperty("失败错误堆栈消息")
    private String errorStack;
}
