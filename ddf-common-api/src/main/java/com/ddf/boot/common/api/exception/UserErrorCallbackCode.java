package com.ddf.boot.common.api.exception;

/**
 * <p>用户账户体系异常定义</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/06/28 15:07
 */
public enum UserErrorCallbackCode implements BaseCallbackCode {

    // 枚举定义里也可以不指定状态码，这样就会使用每个自定义异常内的通用状态码 com.ddf.boot.common.core.exception200.BaseException.defaultCallback

    /**
     * 用户异常体系状态码定义
     */
    USER_NOT_EXIST("U0001", "用户名不存在"),

    PASSWORD_ERROR("U0002", "密码不正确"),


    ;

    /**
     * 异常code码
     */
    private final String code;

    /**
     * 异常消息
     */
    private final String description;



    UserErrorCallbackCode(String description) {
        this.code = null;
        this.description = description;
    }

    UserErrorCallbackCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 响应状态码
     *
     * @return
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 响应消息
     *
     * @return
     */
    @Override
    public String getDescription() {
        return description;
    }
}
