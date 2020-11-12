package com.ddf.boot.common.core.exception200;

/**
 * <p>通用业务异常，只提供预定义的消息状态码构造函数</p >
 *
 * @author dongfang.ding
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

    public BusinessException(String code, String description) {
        super(code, description);
    }

    public BusinessException(String code, String description, Object... params) {
        super(code, description, params);
    }

    /**
     * 提供一种消息占位符的方式， baseCallbackCode中的message包含占位符， 使用的时候格式化参数后作为最终异常消息
     * @param baseCallbackCode
     * @param params
     */
    public BusinessException(BaseCallbackCode baseCallbackCode, Object... params) {
        super(baseCallbackCode, params);
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
