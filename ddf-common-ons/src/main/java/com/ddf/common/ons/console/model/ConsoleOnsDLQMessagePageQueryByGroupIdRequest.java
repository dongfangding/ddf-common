// This file is auto-generated, don't edit it. Thanks.
package com.ddf.common.ons.console.model;

import com.aliyun.ons20190214.models.OnsDLQMessagePageQueryByGroupIdRequest;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;


/**
 * <p>查询GroupId下所有死信消息</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/24 13:44
 */
@Data
public class ConsoleOnsDLQMessagePageQueryByGroupIdRequest implements UserRequest, Serializable {

    private static final long serialVersionUID = -8160563221855666519L;

    /**
     * 获取当前用户
     */
    private String currentUser;

    /**
     * Group_Id
     */
    @NotBlank(message = "GroupId不能为空")
    public String groupId;

    /**
     * 查询范围的起始时间戳
     */
    @NotNull(message = "查询范围的起始时间戳不能为空")
    public Long beginTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24);

    /**
     * 查询范围的终止时间戳
     */
    @NotNull(message = "查询范围的终止时间戳不能为空")
    public Long endTime = System.currentTimeMillis();

    /**
     * 查询任务的ID，首次查询不需要输入，后续取消息必须传入，根据前一次的返回结果取出该字段。
     */
    public String taskId;

    /**
     * 当前取第几页消息
     */
    @NotNull(message = "消息页码不能为空")
    @Min(value = 1, message = "页数最小为1")
    public Integer currentPage = 1;

    /**
     * 分页查询，每页最多显示消息数量
     */
    @NotNull(message = "每页最多显示消息数量不能为空")
    @Max(value = 10, message = "每页最多显示10条数据")
    public Integer pageSize = 10;

    public OnsDLQMessagePageQueryByGroupIdRequest toSdkRequest(String instanceId) {
        return new OnsDLQMessagePageQueryByGroupIdRequest()
                .setGroupId(groupId)
                .setBeginTime(beginTime)
                .setEndTime(endTime)
                .setTaskId(taskId)
                .setCurrentPage(currentPage)
                .setPageSize(pageSize)
                .setInstanceId(instanceId);
    }

}
