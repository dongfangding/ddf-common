package com.ddf.boot.common.exception200;

/**
 * <p>用户账户体系异常定义</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/06/28 15:07
 */
public enum UserErrorCallbackCode implements BaseCallbackCode {

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
