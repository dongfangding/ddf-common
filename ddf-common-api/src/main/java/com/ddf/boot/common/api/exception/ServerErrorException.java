package com.ddf.boot.common.api.exception;

/**
 * <p>服务端异常, 这个异常用在系统内部细节的报错，这些异常在反馈给用户的时候都会被模糊化</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/06/28 13:30
 */
public class ServerErrorException extends BaseException {

    public ServerErrorException(Throwable throwable) {
        super(throwable);
    }

    public ServerErrorException(BaseCallbackCode baseCallbackCode) {
        super(baseCallbackCode);
    }


    public ServerErrorException(String description) {
        super(description);
    }

    public ServerErrorException(String code, String description) {
        super(code, description);
    }

    public ServerErrorException(String code, String description, Object... params) {
        super(code, description, params);
    }

    /**
     * 提供一种消息占位符的方式， baseCallbackCode中的message包含占位符， 使用的时候格式化参数后作为最终异常消息
     *
     * @param baseCallbackCode
     * @param params
     */
    public ServerErrorException(BaseCallbackCode baseCallbackCode, Object... params) {
        super(baseCallbackCode, params);
    }

    /**
     * 当前异常默认响应状态码，作用如下
     * 1. 当抛出异常时没有指定错误码，使用该默认错误码
     * 2. 当异常消息返回给客户端时，使用该错误码的bizMessage来代替原始异常内容返回给客户端，用来隐藏系统异常信息
     *
     * @return
     */
    @Override
    public BaseCallbackCode defaultCallback() {
        return BaseErrorCallbackCode.SERVER_ERROR;
    }

    @Override
    public boolean isMaskErrorDetails() {
        return true;
    }
}
