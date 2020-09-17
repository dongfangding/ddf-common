package com.ddf.boot.common.core.exception200;

/**
 * <p>通用业务异常，只提供预定义的消息状态码构造函数</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/06/28 15:13
 */
public class BusinessException extends BaseException {

    /**
     * @param baseCallbackCode
     */
    public BusinessException(BaseCallbackCode baseCallbackCode) {
        super(baseCallbackCode);
    }

    public BusinessException(String description) {
        super(description);
    }


    /**
     * 当前异常默认响应状态码
     *
     * @return
     */
    @Override
    public BaseCallbackCode defaultCallback() {
        return BaseErrorCallbackCode.SERVER_ERROR;
    }
}
