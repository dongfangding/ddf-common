package com.ddf.common.boot.mqtt.model.request;

import com.ddf.common.boot.mqtt.model.support.header.MqttBaseHeader;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * <p>mqtt 请求头 请求对象 </p >
 *
 * 调用方可以在请求头里放入一些自己需要的数据，也可以放入一些唯一标识符之类的数据方便消息追溯
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/19 11:29
 */
@Data
public class MqttBaseHeaderRequest extends MqttBaseHeader implements Serializable {

    @Serial
    private static final long serialVersionUID = 4813011310202804454L;

}
