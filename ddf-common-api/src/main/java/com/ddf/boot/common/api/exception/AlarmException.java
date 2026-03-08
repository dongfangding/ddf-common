package com.ddf.boot.common.api.exception;

/**
 * <p>告警异常， 这个只是一个约定， 抛出这个异常，在全局异常处理里可以做其它推送告警</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2020/06/28 15:13
 */
public class AlarmException extends BaseException {
    public AlarmException(Throwable throwable) {
        super(throwable);
    }

    /**
     * @param baseCallbackCode
     */
    public AlarmException(BaseCallbackCode baseCallbackCode) {
        super(baseCallbackCode);
    }
    /**
     * @param description 参数
     */
    public AlarmException(String description) {
        super(description);
    }
    /**
     * @param code 参数
     * @param description 参数
     */
    public AlarmException(String code, String description) {
        super(code, description);
    }
    /**
     * @param code 参数
     * @param description 参数
     * @param params 参数
     */
    public AlarmException(String code, String description, Object... params) {
        super(code, description, params);
    }

    /**
     * 提供一种消息占位符的方式， baseCallbackCode中的message包含占位符， 使用的时候格式化参数后作为最终异常消息
     *
     * @param baseCallbackCode
     * @param params
     */
    public AlarmException(BaseCallbackCode baseCallbackCode, Object... params) {
        super(baseCallbackCode, params);
    }

    /**
     * 提供一种消息占位符的方式， baseCallbackCode中的message包含占位符， 使用的时候格式化参数后作为最终异常消息
     *
     * @param extra
     * @param baseCallbackCode
     * @param params
     */
    public AlarmException(Object extra, BaseCallbackCode baseCallbackCode, Object... params) {
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