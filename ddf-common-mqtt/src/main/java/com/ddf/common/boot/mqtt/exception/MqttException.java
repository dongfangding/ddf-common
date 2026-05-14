package com.ddf.common.boot.mqtt.exception;

import com.ddf.boot.common.api.exception.BaseCallbackCode;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BaseException;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/03/18 14:29
 */
public class MqttException extends BaseException {

    /**
     * 只简单抛出消息异常
     *
     * @param description 描述信息
     */
    public MqttException(String description) {
        super(description);
    }

    /**
     * 当前异常默认响应状态码
     */
    @Override
    public BaseCallbackCode defaultCallback() {
        return BaseErrorCallbackCode.DEMO_BLA_BLA;
    }

    @Override
    public boolean isMaskErrorDetails() {
        return true;
    }
}
