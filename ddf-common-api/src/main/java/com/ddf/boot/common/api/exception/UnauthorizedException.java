package com.ddf.boot.common.api.exception;

/**
 * <p>未通过认证异常，通常用于登录认证</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/06/28 13:30
 */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException(Throwable throwable) {
        super(throwable);
    }

    public UnauthorizedException(BaseCallbackCode baseCallbackCode) {
        super(baseCallbackCode);
    }


    public UnauthorizedException(String description) {
        super(description);
    }

    public UnauthorizedException(String code, String description) {
        super(code, description);
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
        return BaseErrorCallbackCode.UNAUTHORIZED;
    }

    @Override
    public boolean isMaskErrorDetails() {
        return false;
    }
}
