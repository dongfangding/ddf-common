package com.ddf.common.boot.mqtt.model.request.emq;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * <p>获取emq连接请求对象</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/03/22 14:54
 */
@Data
public class ConnectionInfoRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 4198990057387176980L;

    /**
     * 需要哪个协议的地址，参见 {@code MQTTProtocolEnum#getProtocol()}
     */
    @NotBlank(message = "协议不能为空")
    @Size(max = 16, message = "协议地址过长，不合法")
    private String protocol;
}
