package com.ddf.common.boot.mqtt.model.request;

import com.ddf.common.boot.mqtt.define.MqttTopicType;
import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * <p>创建topic请求类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 15:43
 */
@Builder
@Data
public class MqttTopicCreateRequest implements Serializable {

    private static final long serialVersionUID = 1516322558409231083L;

    /**
     * topic类型
     */
    @NotNull(message = "topic类型不能为空")
    private MqttTopicType topicType;

    /**
     * topic id 即业务自己的id, 如用户id, 直播间id, 和topicType组合生成完成的topic
     */
    @NotBlank(message = "topicId不能为空")
    @Size(min = 1, max = 128, message = "topicId参数过长")
    private String topicId;
}
