package com.ddf.common.boot.mqtt.model.request.emq;

import com.ddf.common.boot.mqtt.enume.MQTTProtocolEnum;
import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * <p>获取emq连接请求对象</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/22 14:54
 */
@Data
public class ConnectionInfoRequest implements Serializable {

    private static final long serialVersionUID = 4198990057387176980L;

    /**
     * 需要哪个协议的地址
     * @see MQTTProtocolEnum#getProtocol()
     */
    @NotBlank(message = "协议不能为空")
    @Size(max = 16, message = "协议地址过长，不合法")
    private String protocol;
}
