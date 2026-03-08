package com.ddf.boot.common.api.exception;

/**
 * <p>访问被拒绝，通常用于权限判断</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2020/06/28 13:28
 */
public class AccessDeniedException extends BaseException {
    public AccessDeniedException(Throwable throwable) {
        super(throwable);
    }
    /**
     * @param baseCallbackCode 参数
     */
    public AccessDeniedException(BaseCallbackCode baseCallbackCode) {
        super(baseCallbackCode);
    }
    /**
     * @param description 参数
     */
    public AccessDeniedException(String description) {
        super(description);
    }
    /**
     * @param code 参数
     * @param description 参数
     */
    public AccessDeniedException(String code, String description) {
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
        return BaseErrorCallbackCode.ACCESS_FORBIDDEN;
    }

    @Override
    public boolean isMaskErrorDetails() {
        return false;
    }
}