package com.ddf.common.ons.console.model.response;

import com.aliyun.ons20190214.models.OnsTopicSubDetailResponse;
import com.aliyun.ons20190214.models.OnsTopicSubDetailResponseBody;
import com.ddf.common.ons.enume.MessageModel;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/25 16:05
 */
@Data
@Accessors(chain = true)
public class ConsoleOnsTopicSubListResponse implements Serializable {

    private static final long serialVersionUID = 2847357762600183099L;

    /**
     * Group Id
     */
    public String groupId;

    /**
     * 消费模式代码
     */
    public String messageModel;

    /**
     * 消费模式名称
     */
    private String messageModelName;

    /**
     * 订阅表达式
     */
    public String expression;

    /**
     * 从SDK转换
     */
    public static List<ConsoleOnsTopicSubListResponse> convertFromSdk(OnsTopicSubDetailResponse response) {
        return response.getBody().getData().getSubscriptionDataList().getSubscriptionDataList().stream()
                .map(ConsoleOnsTopicSubListResponse::convert).collect(Collectors.toList());
    }


    public static ConsoleOnsTopicSubListResponse convert(
            OnsTopicSubDetailResponseBody.OnsTopicSubDetailResponseBodyDataSubscriptionDataListSubscriptionDataList info) {
        return new ConsoleOnsTopicSubListResponse()
                .setGroupId(info.getGroupId())
                .setMessageModel(info.getMessageModel())
                .setMessageModelName(MessageModel.getDescByModel(info.getMessageModel()))
                .setExpression(info.getSubString());
    }
}
