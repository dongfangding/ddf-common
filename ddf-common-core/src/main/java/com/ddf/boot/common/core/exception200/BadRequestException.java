package com.ddf.boot.common.core.exception200;

/**
 * <p>错误请求，通常用于处理接口请求</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/06/28 13:14
 */
public class BadRequestException extends BaseException {

    public BadRequestException(Throwable throwable) {
        super(throwable);
    }

    public BadRequestException(BaseCallbackCode baseCallbackCode) {
        super(baseCallbackCode);
    }


    public BadRequestException(String description) {
        super(description);
    }

    public BadRequestException(String code, String description) {
        super(code, description);
    }


    public BadRequestException(String code, String description, Object... params) {
        super(code, description, params);
    }

    /**
     * 提供一种消息占位符的方式， baseCallbackCode中的message包含占位符， 使用的时候格式化参数后作为最终异常消息
     *
     * @param baseCallbackCode
     * @param params
     */
    public BadRequestException(BaseCallbackCode baseCallbackCode, Object... params) {
        super(baseCallbackCode, params);
    }


    /**
     * 当前异常默认响应状态码
     *
     * @return
     */
    @Override
    public BaseCallbackCode defaultCallback() {
        return BaseErrorCallbackCode.BAD_REQUEST;
    }
}
