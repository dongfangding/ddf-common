package com.ddf.boot.common.api.exception;

/**
 * 通用业务异常，这个异常用在一般是业务上的规则不允许的操作
 * 这个异常用在打的消息都是反馈给用户看的，否则不要使用这个异常，如系统内部或者框架层面的不要使用这个异常
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2020/06/28 15:13
 */
public class BusinessException extends BaseException {
    public BusinessException(Throwable throwable) {
        super(throwable);
    }

    /**
     * @param baseCallbackCode
     */
    public BusinessException(BaseCallbackCode baseCallbackCode) {
        super(baseCallbackCode);
    }
    /**
     * @param description 参数
     */
    public BusinessException(String description) {
        super(description);
    }
    /**
     * @param code 参数
     * @param description 参数
     */
    public BusinessException(String code, String description) {
        super(code, description);
    }
    /**
     * @param code 参数
     * @param description 参数
     * @param params 参数
     */
    public BusinessException(String code, String description, Object... params) {
        super(code, description, params);
    }

    /**
     * 提供一种消息占位符的方式， baseCallbackCode中的message包含占位符， 使用的时候格式化参数后作为最终异常消息
     *
     * @param baseCallbackCode
     * @param params
     */
    public BusinessException(BaseCallbackCode baseCallbackCode, Object... params) {
        super(baseCallbackCode, params);
    }

    /**
     * 提供一种消息占位符的方式， baseCallbackCode中的message包含占位符， 使用的时候格式化参数后作为最终异常消息
     *
     * @param extra
     * @param baseCallbackCode
     * @param params
     */
    public BusinessException(Object extra, BaseCallbackCode baseCallbackCode, Object... params) {
        super(extra, baseCallbackCode, params);
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
        return BaseErrorCallbackCode.BIZ_EXCEPTION;
    }

    @Override
    public boolean isMaskErrorDetails() {
        return false;
    }
}