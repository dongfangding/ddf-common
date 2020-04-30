package com.ddf.boot.common.websocket.model.ws;

import com.ddf.boot.common.exception.GlobalCustomizeException;
import com.ddf.boot.common.websocket.enumerate.CmdEnum;
import com.ddf.boot.common.websocket.model.payload.ChildCmdPayload;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * 指令发送的请求参数
 *
 * @author dongfang.ding
 * @date 2019/9/24 16:17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("请求对象")
@Accessors(chain = true)
public class MessageRequest<T> implements Serializable {

    @ApiModelProperty(value = "命令码", required = true)
    private CmdEnum cmd;

    @ApiModelProperty(value = "和设备绑定的token", required = true)
    private String token;

    @ApiModelProperty(value = "设备号", required = true)
    private String ime;

    @ApiModelProperty(value = "客户端渠道，想要upay在哪个应用上执行业务，默认upay,但是为了防止错误，服务端不会给默认值，而是让客户端明确", required = true)
    private ClientChannel clientChannel;

    @ApiModelProperty(required = true, value = "发送指令的模式，为了防止通过token和ime参数的判断来确定是群发还是单发" +
            "还是批量发的误传，因此需要参数和模式互相对应")
    private SendMode sendMode;

    @ApiModelProperty("业务主键，服务端根据这个值和cmd判断客户端是否对同一条业务数据发送相同的命令码")
    private String logicPrimaryKey;


    @ApiModelProperty(required = true, value = "主体数据")
    private T payload;

    @ApiModelProperty("服务端用来存储发送时携带的业务主键，方便数据回传时，不依赖于客户端将业务数据回传")
    private String businessData;

    /**
     * 后端调用接口的传入这个参数
     */
    @ApiModelProperty(value = "操作人id")
    private String operatorId;

    @ApiModelProperty(value = "是否需要检测最后一次发送时间")
    private boolean checkLastTime;

    @ApiModelProperty(value = "发送间隔，单位为分钟。如果上次发送(指令处理结果为成功的才计入)时间距离现在没有达到间隔，则不会实际发送指令")
    private int sendMinutesInterval;

    @ApiModelProperty(value = "每天发送最大次数，如果满足了发送间隔，但次数已经达到了（指令处理结果为成功的才计入）依然不会发送，如果为0，则是不显示次数")
    private int dailyMaxTimes;

    @ApiModelProperty(value = "是否需要异步处理该接口，默认同步；群发指令或批量指令该参数无效，服务端会强制只能异步")
    private boolean async;

    @ApiModelProperty(value = "同步的阻塞时间, 单位毫秒，默认10000，服务端控制最大30000")
    private long blockMilliSeconds = 10000;

    @ApiModelProperty("是否需要附加支付方式的账号信息到请求头，这么做调用方想自定义传参就很麻烦了；单现在系统多个地方都有，" +
            "因此简单方便想定义参数统一处理，额外再提供一个自定义请求头的方法")
    private boolean appendAccountInfo;

    /**
     * 不需要调用方处理这个参数
     */
    @ApiModelProperty("是否转发,默认false,如果接收到指令的机器处理不了这个指令，就会把这个参数改成true，是true的话别的机器就不需要再转发了")
    private boolean redirect;

    /**
     * 不需要调用方处理这个参数
     */
    @ApiModelProperty("由哪台服务器转发过来，如果是群发消息的话，自己转发过，但同时也处理过，不能重复处理")
    private String redirectFrom;

    /**
     * 不需要调用方处理这个参数
     */
    @ApiModelProperty("可用连接在哪台服务器上")
    private String socketSessionOn;



    /**
     * 不需要调用方处理这个参数
     *
     * 由于需要实现一个阻塞的实现，两个线程之间请求和响应对应是通过requestId来支持的，
     * 如果是集群环境下，转发的机器需要先生成数据，这样才能保证转发的服务器生成的数据返回回来能够
     * 和请求这里对应起来
     *
     */
    @ApiModelProperty("转发的报文数据")
    private String message;


    /**
     * 指令的发送模式，模式还必须有正确的参数配合
     */
    public enum SendMode {
        /** 对单个设备发送指令 */
        SINGLE,
        /** 批量 */
        BATCH,
        /** 全部 */
        ALL
    }


    /**
     * 提供一个方法方便调用方处理处理子命令码
     *
     * @param childEnum
     * @return

     * @date 2019/9/24 18:57
     */
    public static void userSimple(MessageRequest<ChildCmdPayload> messageRequest
            , CmdEnum.ChildEnum childEnum) {
        if (!CmdEnum.SIMPLE.equals(messageRequest.getCmd())) {
            throw new GlobalCustomizeException("非SIMPLE指令码，不允许使用子命令码功能!");
        }
        messageRequest.setCmd(CmdEnum.SIMPLE);
        if (childEnum == null) {
            throw new GlobalCustomizeException("子命令码不能为空!");
        }
        ChildCmdPayload childCmdPayload = new ChildCmdPayload();
        childCmdPayload.setChildCmd(childEnum);
        messageRequest.setPayload(childCmdPayload);
    }

    /**
     * 单设备指令
     *
     * @param
     * @return

     * @date 2019/9/25 10:25
     */
    public MessageRequest<T> toSingle() {
        return this.setSendMode(SendMode.SINGLE);
    }

    /**
     * 批量设备指令
     *
     * @param
     * @return

     * @date 2019/9/25 10:25
     */
    public MessageRequest<T> toBatch() {
        return this.setSendMode(SendMode.BATCH);
    }

    /**
     * 全设备指令
     *
     * @param
     * @return

     * @date 2019/9/25 10:25
     */
    public MessageRequest<T> toAll() {
        return this.setSendMode(SendMode.ALL);
    }
}
