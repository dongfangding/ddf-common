package com.ddf.common.ons.console.model.response;

import com.aliyun.ons20190214.models.OnsTopicListResponse;
import com.aliyun.ons20190214.models.OnsTopicListResponseBody;
import com.ddf.common.ons.console.constant.TopicMessageType;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 查询账号下所有 Topic 的信息列表响应类
 * 这里做了属性精简
 * </p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/25 14:38
 */
@Data
@Accessors(chain = true)
public class ConsoleOnsTopicListResponse implements Serializable {

    /**
     * 消息类型
     */
    public Integer messageType;

    /**
     * 消息类型名称
     */
    private String messageTypeName;

    /**
     * 所有关系名称
     */
    public String relationName;

    /**
     * Topic所有者编号
     */
    public String owner;

    /**
     * Topic所在实例是否有命名空间
     */
    public Boolean independentNaming;

    /**
     * 备注信息。
     */
    public String remark;

    /**
     * 所有关系名称
     */
    public Integer relation;

    /**
     * 创建时间
     */
    public Long createTime;

    /**
     * Topic 名称
     */
    public String topic;

    /**
     * 实例id
     */
    public String instanceId;

    public static List<ConsoleOnsTopicListResponse> convertFromSdk(OnsTopicListResponse sdkResponse) {
        return sdkResponse.getBody()
                .getData()
                .getPublishInfoDo().stream().map(ConsoleOnsTopicListResponse::convert).collect(Collectors.toList());
    }

    public static ConsoleOnsTopicListResponse convert(
            OnsTopicListResponseBody.OnsTopicListResponseBodyDataPublishInfoDo infoDo) {
        return new ConsoleOnsTopicListResponse().setMessageType(infoDo.getMessageType())
                .setMessageTypeName(TopicMessageType.getDesc(infoDo.getMessageType()))
                .setRelationName(infoDo.getRelationName())
                .setOwner(infoDo.getOwner())
                .setIndependentNaming(infoDo.getIndependentNaming())
                .setRemark(infoDo.getRemark())
                .setRelation(infoDo.getRelation())
                .setCreateTime(infoDo.getCreateTime())
                .setTopic(infoDo.getTopic())
                .setInstanceId(infoDo.getInstanceId());
    }
}
