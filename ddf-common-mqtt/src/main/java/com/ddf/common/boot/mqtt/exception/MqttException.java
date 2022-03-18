package com.ddf.common.boot.mqtt.exception;

import com.ddf.boot.common.core.exception200.BaseCallbackCode;
import com.ddf.boot.common.core.exception200.BaseErrorCallbackCode;
import com.ddf.boot.common.core.exception200.BaseException;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/18 14:29
 */
public class MqttException extends BaseException {

    /**
     * 只简单抛出消息异常
     *
     * @param description
     */
    public MqttException(String description) {
        super(description);
    }

    /**
     * 当前异常默认响应状态码
     *
     * @return
     */
    @Override
    public BaseCallbackCode defaultCallback() {
        return BaseErrorCallbackCode.DEMO_BLA_BLA;
    }
}
