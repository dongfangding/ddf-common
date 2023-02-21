package com.ddf.common.ons.console.model;

import com.ddf.common.ons.console.constant.RetryChannelEnum;
import com.google.common.base.Objects;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>从日志中记录执行重试</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/05/17 16:33
 */
@Data
public class LogRetryRequest implements UserRequest {

    /**
     * 获取当前用户
     */
    private String currentUser;

    @NotEmpty(message = "重试的id不能为空")
    @Size(max = 10, message = "由于业务方QPS限制，一次最多可以选择10条数据进行重试")
    private List<String> objectIdList;

    /**
     * GroupId 死信重推的时候必填
     */
    private String groupId;

    /**
     * 重试通道
     */
    private RetryChannelEnum retryChannel = RetryChannelEnum.ONS_MESSAGE_PUSH;

    public void checkRequired() {
        if (Objects.equal(RetryChannelEnum.ONS_DLQ_MESSAGE_RESEND_BY_ID, retryChannel)) {
            if (StringUtils.isBlank(groupId)) {
                throw new IllegalArgumentException("死信重新消费GroupId不能为空");
            }
        }
    }
}
