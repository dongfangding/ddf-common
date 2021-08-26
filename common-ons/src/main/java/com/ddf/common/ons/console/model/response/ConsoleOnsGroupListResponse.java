package com.ddf.common.ons.console.model.response;

import com.aliyun.ons20190214.models.OnsGroupListResponse;
import com.aliyun.ons20190214.models.OnsGroupListResponseBody;
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
 * @date 2021/05/25 15:41
 */
@Data
@Accessors(chain = true)
public class ConsoleOnsGroupListResponse implements Serializable {

    private static final long serialVersionUID = -7844010129175423472L;

    /**
     * 该发布信息的拥有者。
     */
    public String owner;

    /**
     * 该Group ID的更新时间。
     */
    public Long updateTime;

    /**
     * 实例是否有命名空间。
     */
    public Boolean independentNaming;

    /**
     * 消费集群组ID。
     */
    public String groupId;

    /**
     * 备注。
     */
    public String remark;

    /**
     * GroupID的创建时间。。
     */
    public Long createTime;

    /**
     * 实例ID。。
     */
    public String instanceId;

    /**
     * 协议
     */
    public String groupType;


    /**
     * 从SDK响应对象解析
     *
     * @param response
     * @return
     */
    public static List<ConsoleOnsGroupListResponse> convertFromSdk(OnsGroupListResponse response) {
        return response.getBody().getData().getSubscribeInfoDo().stream().map(ConsoleOnsGroupListResponse::convert)
                .collect(Collectors.toList());

    }

    public static ConsoleOnsGroupListResponse convert(
            OnsGroupListResponseBody.OnsGroupListResponseBodyDataSubscribeInfoDo infoDo) {
        return new ConsoleOnsGroupListResponse().setOwner(infoDo.getOwner())
                .setUpdateTime(infoDo.getUpdateTime())
                .setIndependentNaming(infoDo.getIndependentNaming())
                .setGroupId(infoDo.getGroupId())
                .setRemark(infoDo.getRemark())
                .setCreateTime(infoDo.getCreateTime())
                .setInstanceId(infoDo.getInstanceId())
                .setGroupType(infoDo.getGroupType());
    }
}
