package com.ddf.common.ons.producer;

import com.ddf.common.ons.console.util.OnsConsoleUtil;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

/**
 *
 * ONS消息对象
 *
 * @author snowball
 * @date 2021/8/26 14:51
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnsMessage implements Serializable {

    private static final long serialVersionUID = 5484128488254938570L;

    public static final String WRAPPER_BIZ_ID_SEPARATOR = "#";

    /**
     * 主题
     */
    private String topic;
    /**
     * 路由表达式
     */
    private String expression;
    /**
     * 消息体
     */
    private String payLoad;
    /**
     * 业务Id，每次发送必须唯一
     */
    private String bizId;
    /**
     * 顺序消息必传
     * 分区顺序消息中区分不同分区的关键字段，Sharding Key 与普通消息的 key 是完全不同的概念。
     * 全局顺序消息，该字段可以设置为任意非空字符串。
     */
    private String shadingKey;
    /**
     * 发送延时消息的延时时间，单位毫秒
     * 如果是定时消息，需要发送方自己把时间转换成毫秒数
     */
    private Long delayTime;

    public void check(){
        Assert.hasLength(getTopic(), "Topic不能为空");
        Assert.hasLength(getExpression(), "Expression不能为空");
        Assert.hasLength(getPayLoad(), "PayLoad不能为空");
        Assert.hasLength(getBizId(), "BizId不能为空");
    }

    public void checkOrder(){
        check();
        Assert.hasLength(getShadingKey(), "ShadingKey不能为空");
    }

    public String generateBizId() {
        return System.currentTimeMillis() + "-" + getBizId();
    }

    public String getWrapperBizId() {
        if (getBizId().contains(WRAPPER_BIZ_ID_SEPARATOR)) {
            return getBizId();
        }
        return OnsConsoleUtil.getShortNameBySplit(getExpression(), "_", "")
                + WRAPPER_BIZ_ID_SEPARATOR + getBizId();
    }
}
